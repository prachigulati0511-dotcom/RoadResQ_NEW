/**
 * RoadResQ Provider Portal
 * Manages incoming emergency request broadcast pool, atomic First-Provider-Wins job acceptance,
 * active job management, status transitions, and revised quote submission
 */

const Provider = {
  activeJob: null,
  pendingRequests: [],
  pollTimer: null,
  isOnline: true,
  knownPendingIds: new Set(),

  init() {
    this.bindEvents();
    this.startRadarPolling();
  },

  bindEvents() {
    const statusToggle = document.getElementById('provider-online-toggle');
    if (statusToggle) {
      statusToggle.addEventListener('change', (e) => {
        this.toggleOnline(e.target.checked);
      });
    }
  },

  startRadarPolling() {
    if (this.pollTimer) clearInterval(this.pollTimer);
    this.loadIncomingRequests();
    this.loadMyActiveJobs();

    // Auto-poll every 3 seconds for new emergencies or acceptance updates
    this.pollTimer = setInterval(() => {
      if (Auth.currentUser && Auth.currentUser.role === 'PROVIDER' && this.isOnline) {
        this.loadIncomingRequests();
        this.loadMyActiveJobs();
      }
    }, 3000);
  },

  async toggleOnline(online) {
    this.isOnline = online;
    try {
      await API.post('/api/provider/status', { isOnline: online });
      App.showToast(online ? 'You are now ONLINE and accepting rescue dispatches!' : 'You are now OFFLINE', online ? 'success' : 'info');
      const label = document.getElementById('provider-status-label');
      if (label) {
        label.textContent = online ? 'Online & Active' : 'Offline';
        label.style.color = online ? 'var(--teal-neon)' : 'var(--text-muted)';
      }
    } catch (err) {
      App.showToast('Failed to toggle status: ' + err.message, 'error');
    }
  },

  async loadIncomingRequests() {
    try {
      const requests = await API.get('/api/provider/pending-requests');
      this.pendingRequests = requests;

      // Play audio chime if a brand new emergency arrived
      requests.forEach(r => {
        if (!this.knownPendingIds.has(r.id)) {
          this.knownPendingIds.add(r.id);
          App.playChime(920, 0.15);
          App.showToast(`New Emergency Request Broadcast: ${r.serviceTypeDisplay} near ${r.address}`, 'warning');
        }
      });

      this.renderIncomingRequests(requests);
    } catch (err) {
      console.warn('Failed to load pending requests:', err);
    }
  },

  async loadMyActiveJobs() {
    try {
      const myJobs = await API.get('/api/provider/my-jobs');
      const inProgress = myJobs.find(j => j.status !== 'COMPLETED' && j.status !== 'CANCELLED');
      this.activeJob = inProgress || null;
      this.renderActiveJob(this.activeJob);
      this.renderCompletedStats(myJobs);
    } catch (err) {
      console.warn('Failed to load provider jobs:', err);
    }
  },

  renderIncomingRequests(list) {
    const container = document.getElementById('provider-incoming-pool');
    const badgeCount = document.getElementById('provider-incoming-count');
    if (badgeCount) badgeCount.textContent = list.length;

    if (!container) return;

    if (list.length === 0) {
      container.innerHTML = `
        <div class="glass-panel" style="grid-column: 1 / -1; padding:2.5rem; text-align:center; color:var(--text-muted);">
          <div style="font-size:2rem; margin-bottom:0.5rem;">&#128225;</div>
          <div style="font-weight:700; color:#fff; font-size:1.1rem; margin-bottom:0.3rem;">Rescue Radar Active & Listening</div>
          <div style="font-size:0.85rem;">All regional emergency calls are currently assigned. Standing by for dispatch signals...</div>
        </div>
      `;
      return;
    }

    container.innerHTML = list.map(req => {
      const isUrgent = req.severity === 'CRITICAL_HAZARD';
      return `
        <div class="incoming-request-card ${isUrgent ? 'urgent-hazard' : ''}">
          <div style="display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:1rem;">
            <div>
              <span class="role-tag ${isUrgent ? 'role-provider' : 'role-customer'}">${req.severity.replace('_', ' ')}</span>
              <h3 style="font-size:1.2rem; font-weight:800; color:#fff; margin-top:0.4rem;">${req.serviceTypeDisplay}</h3>
              <div style="font-size:0.82rem; color:var(--text-secondary); margin-top:2px;">
                Customer: <strong style="color:#fff;">${req.customerName}</strong> &bull; ${req.vehicleInfo || 'Vehicle'}
              </div>
            </div>
            <div style="text-align:right;">
              <div style="font-size:0.75rem; color:var(--text-muted);">Est. Payout</div>
              <div style="font-size:1.3rem; font-weight:800; color:var(--teal-neon);">₹${App.formatINR(req.totalEstimatedCost)}</div>
            </div>
          </div>

          <div style="background:rgba(0,0,0,0.3); border:1px solid var(--border-subtle); border-radius:8px; padding:0.85rem; margin-bottom:1.25rem; font-size:0.85rem;">
            <div style="display:flex; align-items:center; gap:0.4rem; color:var(--text-secondary); margin-bottom:0.3rem;">
              <i data-lucide="map-pin" style="color:var(--teal-neon); width:14px; height:14px;"></i>
              <span style="color:#fff;">${req.address}</span>
            </div>
            ${req.problemDescription ? `
              <div style="color:var(--text-muted); font-size:0.8rem; margin-top:4px;">
                "${req.problemDescription.substring(0, 100)}${req.problemDescription.length > 100 ? '...' : ''}"
              </div>
            ` : ''}
          </div>

          ${req.photoUrl ? `
            <div style="margin-bottom:1.25rem;">
              <img src="${API.resolveUrl(req.photoUrl)}" alt="Customer problem photo" style="height:90px; border-radius:6px; border:1px solid var(--border-teal); cursor:pointer;" onclick="App.openLightbox('${API.resolveUrl(req.photoUrl)}')" />
              <span style="font-size:0.75rem; color:var(--text-muted); margin-left:6px;">Click to inspect issue</span>
            </div>
          ` : ''}

          <div style="display:flex; gap:0.75rem;">
            <!-- ATOMIC FIRST-PROVIDER-WINS ACCEPT BUTTON -->
            <button class="btn-primary btn-sm" onclick="Provider.acceptRequest(${req.id})" style="flex:1;">
              <i data-lucide="check"></i> Accept Job
            </button>
            <button class="btn-secondary btn-sm" onclick="Provider.dismissFromLocalPool(${req.id})" title="Pass / Ignore">
              <i data-lucide="x"></i>
            </button>
          </div>
        </div>
      `;
    }).join('');

    if (window.lucide) lucide.createIcons();
  },

  /**
   * ATOMIC FIRST-PROVIDER-WINS ACCEPTANCE ACTION
   * Executes atomic acceptance on the backend.
   * If another provider accepted it first, handles HTTP 409 Conflict gracefully!
   */
  async acceptRequest(requestId) {
    App.showLoading(true, 'Locking in emergency dispatch assignment...');

    try {
      const assigned = await API.post(`/api/provider/requests/${requestId}/accept`);
      App.playChime(660, 0.2);
      App.showToast(`Job ${assigned.trackingCode} successfully assigned to you!`, 'success');

      // Refresh jobs and pool
      await this.loadMyActiveJobs();
      await this.loadIncomingRequests();
    } catch (err) {
      if (err.status === 409) {
        // First-provider-wins conflict!
        App.playChime(300, 0.2);
        App.showToast('Another rescue unit accepted this job first! Broadcast has been assigned.', 'warning');
        // Instantly remove it from the local list
        this.dismissFromLocalPool(requestId);
      } else {
        App.showToast('Acceptance error: ' + err.message, 'error');
      }
    } finally {
      App.showLoading(false);
    }
  },

  dismissFromLocalPool(id) {
    this.pendingRequests = this.pendingRequests.filter(r => r.id !== id);
    this.renderIncomingRequests(this.pendingRequests);
  },

  renderActiveJob(job) {
    const section = document.getElementById('provider-active-job-section');
    if (!section) return;

    if (!job) {
      section.style.display = 'none';
      return;
    }

    section.style.display = 'block';
    section.innerHTML = `
      <div class="glass-panel-glow" style="padding:2rem; background:rgba(12, 23, 38, 0.95); margin-bottom:2.5rem; border:1px solid var(--border-teal);">
        <div style="display:flex; justify-content:space-between; align-items:flex-start; flex-wrap:wrap; gap:1rem; margin-bottom:1.5rem;">
          <div>
            <div style="display:inline-flex; align-items:center; gap:0.5rem; background:var(--teal-dim); color:var(--teal-neon); border:1px solid var(--border-teal); padding:3px 10px; border-radius:9999px; font-size:0.75rem; font-weight:800; margin-bottom:0.5rem;">
              <span class="sos-pulse-ring" style="width:8px; height:8px;"></span> ACTIVE RESCUE MISSION IN PROGRESS
            </div>
            <h2 style="font-size:1.6rem; font-weight:800; color:#fff;">${job.serviceTypeDisplay}</h2>
            <div style="font-size:0.9rem; color:var(--text-secondary); margin-top:2px;">
              Tracking Code: <strong style="color:var(--teal-neon);">${job.trackingCode}</strong> &bull; Status: <span class="role-tag role-provider">${job.status.replace(/_/g, ' ')}</span>
            </div>
          </div>
          <div style="text-align:right;">
            <div style="font-size:0.8rem; color:var(--text-muted);">Contract Fee</div>
            <div style="font-size:1.8rem; font-weight:800; color:var(--teal-neon);">₹${App.formatINR(job.totalEstimatedCost)}</div>
          </div>
        </div>

        <!-- Customer & Vehicle Info -->
        <div style="display:grid; grid-template-columns:repeat(auto-fit, minmax(260px, 1fr)); gap:1.25rem; margin-bottom:1.5rem; background:rgba(255,255,255,0.03); border:1px solid var(--border-subtle); padding:1.25rem; border-radius:12px;">
          <div>
            <div style="font-size:0.75rem; color:var(--text-muted); text-transform:uppercase; font-weight:700;">Customer Details</div>
            <div style="font-size:1.05rem; font-weight:700; color:#fff; margin-top:2px;">${job.customerName}</div>
            <div style="font-size:0.85rem; color:var(--text-secondary);">${job.customerPhone}</div>
            <a href="tel:${job.customerPhone}" class="btn-secondary btn-sm" style="margin-top:0.6rem; display:inline-flex;">
              <i data-lucide="phone"></i> Call Customer
            </a>
          </div>

          <div>
            <div style="font-size:0.75rem; color:var(--text-muted); text-transform:uppercase; font-weight:700;">Location & Vehicle</div>
            <div style="font-size:0.95rem; font-weight:700; color:#fff; margin-top:2px;">${job.address}</div>
            <div style="font-size:0.85rem; color:var(--teal-neon); margin-top:2px;">Vehicle: ${job.vehicleInfo || 'Standard'}</div>
          </div>

          ${job.photoUrl ? `
            <div>
              <div style="font-size:0.75rem; color:var(--text-muted); text-transform:uppercase; font-weight:700;">Customer Photo</div>
              <img src="${API.resolveUrl(job.photoUrl)}" alt="Issue photo" style="height:70px; border-radius:6px; border:1px solid var(--border-teal); cursor:pointer; margin-top:4px;" onclick="App.openLightbox('${API.resolveUrl(job.photoUrl)}')" />
            </div>
          ` : ''}
        </div>

        <!-- Problem Description -->
        ${job.problemDescription ? `
          <div style="background:rgba(0,0,0,0.3); border:1px solid var(--border-subtle); padding:1rem; border-radius:8px; margin-bottom:1.5rem; font-size:0.88rem; color:var(--text-secondary);">
            <strong style="color:#fff;">Problem Description:</strong> ${job.problemDescription}
          </div>
        ` : ''}

        <!-- Provider Actions & Status Transitions -->
        <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:1rem; border-top:1px solid var(--border-subtle); padding-top:1.5rem;">
          <div style="display:flex; gap:0.75rem; flex-wrap:wrap;">
            ${job.status === 'ACCEPTED' ? `
              <button class="btn-primary" onclick="Provider.updateStatus(${job.id}, 'ON_THE_WAY')">
                <i data-lucide="navigation"></i> Start Driving (On The Way)
              </button>
            ` : ''}

            ${job.status === 'ON_THE_WAY' ? `
              <button class="btn-primary" onclick="Provider.updateStatus(${job.id}, 'ARRIVED')">
                <i data-lucide="map-pin"></i> Mark Arrived on Scene
              </button>
            ` : ''}

            ${job.status === 'ARRIVED' || job.status === 'IN_PROGRESS' ? `
              <button class="btn-primary" onclick="Provider.updateStatus(${job.id}, 'COMPLETED')">
                <i data-lucide="check-circle-2"></i> Complete Service
              </button>
            ` : ''}

            <button class="btn-secondary" onclick="Provider.openRevisedQuoteModal(${job.id})">
              <i data-lucide="file-plus"></i> Submit Revised Quote
            </button>
          </div>

          <div style="font-size:0.8rem; color:var(--text-muted);">
            Customer dashboard automatically updates in real-time.
          </div>
        </div>
      </div>
    `;

    if (window.lucide) lucide.createIcons();
  },

  async updateStatus(jobId, newStatus) {
    try {
      await API.patch(`/api/provider/requests/${jobId}/status`, { status: newStatus });
      App.showToast(`Job status updated to ${newStatus.replace(/_/g, ' ')}`, 'success');
      App.playChime(700, 0.15);
      await this.loadMyActiveJobs();
    } catch (err) {
      App.showToast('Failed to update status: ' + err.message, 'error');
    }
  },

  openRevisedQuoteModal(jobId) {
    const modal = document.getElementById('provider-quote-modal');
    if (!modal) return;
    document.getElementById('quote-job-id').value = jobId;
    App.openModal('provider-quote-modal');
  },

  async submitRevisedQuote() {
    const jobId = document.getElementById('quote-job-id').value;
    const amount = parseFloat(document.getElementById('quote-new-amount').value);
    const reason = document.getElementById('quote-reason-input').value;

    if (!amount || isNaN(amount) || amount <= 0) {
      App.showToast('Please enter a valid amount', 'warning');
      return;
    }

    try {
      await API.post(`/api/provider/requests/${jobId}/quote`, {
        revisedQuote: amount,
        reason: reason || 'On-site parts replacement required'
      });
      App.showToast('Revised quote submitted to customer for approval!', 'success');
      App.closeModal('provider-quote-modal');
      await this.loadMyActiveJobs();
    } catch (err) {
      App.showToast('Failed to submit quote: ' + err.message, 'error');
    }
  },

  renderCompletedStats(jobs) {
    const completed = jobs.filter(j => j.status === 'COMPLETED');
    const totalEarnings = completed.reduce((sum, j) => sum + (j.totalEstimatedCost || 0), 0);

    const countEl = document.getElementById('provider-completed-count');
    const earningsEl = document.getElementById('provider-earnings-count');
    if (countEl) countEl.textContent = completed.length;
    if (earningsEl) earningsEl.textContent = `₹${App.formatINR(totalEarnings)}`;
  }
};

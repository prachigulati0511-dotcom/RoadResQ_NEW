/**
 * RoadResQ Live Request Tracking Module
 * Real-time map visualization with Leaflet, simulated vehicle telemetry navigation,
 * timeline progression, and revised quote approval workflow
 */

const Tracking = {
  currentRequestId: null,
  currentTrackingCode: null,
  pollTimer: null,
  map: null,
  customerMarker: null,
  providerMarker: null,
  routePolyline: null,
  animationTimer: null,
  simulatedProgress: 0.1, // 0 to 1
  latestData: null,

  init() {
    this.initMap();
    // Check if URL has ?track=RR-XXXXXX
    const urlParams = new URLSearchParams(window.location.search);
    const trackCode = urlParams.get('track');
    if (trackCode) {
      this.trackByCode(trackCode);
    }
  },

  initMap() {
    const mapEl = document.getElementById('tracking-live-map');
    if (!mapEl || this.map) return;

    if (typeof L === 'undefined') return;

    this.map = L.map('tracking-live-map', {
      center: [30.1290, 77.2674],
      zoom: 13,
      zoomControl: true
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors',
      maxZoom: 19
    }).addTo(this.map);
  },

  startTracking(requestId, trackingCode) {
    this.currentRequestId = requestId;
    this.currentTrackingCode = trackingCode;
    this.simulatedProgress = 0.1;

    // Clear existing timer
    if (this.pollTimer) clearInterval(this.pollTimer);
    if (this.animationTimer) clearInterval(this.animationTimer);

    // Initial fetch
    this.fetchStatus();

    // Poll every 3 seconds for real-time status transitions
    this.pollTimer = setInterval(() => this.fetchStatus(), 3000);
  },

  async trackByCode(code) {
    App.navigateTo('tracking');
    App.showLoading(true, 'Locating rescue dispatch telemetry...');
    try {
      const data = await API.get(`/api/requests/track/${code}`);
      this.startTracking(data.id, data.trackingCode);
    } catch (err) {
      App.showToast('Could not find active rescue for code: ' + code, 'error');
    } finally {
      App.showLoading(false);
    }
  },

  async fetchStatus() {
    if (!this.currentRequestId && !this.currentTrackingCode) return;

    try {
      let data;
      if (this.currentRequestId) {
        data = await API.get(`/api/requests/${this.currentRequestId}`);
      } else {
        data = await API.get(`/api/requests/track/${this.currentTrackingCode}`);
      }

      const previousStatus = this.latestData ? this.latestData.status : null;
      this.latestData = data;
      this.renderTrackingUI(data);
      this.updateMapVisualization(data);

      // Status change audio and toast notification
      if (previousStatus && previousStatus !== data.status) {
        App.playChime(750, 0.2);
        App.showToast(`Rescue status updated: ${data.status.replace(/_/g, ' ')}`, 'info');

        // If completed, trigger review modal
        if (data.status === 'COMPLETED') {
          setTimeout(() => History.openReviewModal(data.id), 1200);
        }
      }
    } catch (err) {
      console.warn('Tracking poll warning:', err);
    }
  },

  renderTrackingUI(data) {
    // Tracking Code & Share Banner
    const codeEl = document.getElementById('tracking-code-display');
    if (codeEl) codeEl.textContent = data.trackingCode;

    // Timeline Progress Tracker
    this.updateTimelineNodes(data.status);

    // Status Badge
    const badgeEl = document.getElementById('tracking-status-badge');
    if (badgeEl) {
      badgeEl.textContent = data.status.replace(/_/g, ' ');
      badgeEl.className = `role-tag ${data.status === 'COMPLETED' ? 'role-customer' : 'role-provider'}`;
    }

    // ETA & Distance
    const etaValEl = document.getElementById('tracking-eta-val');
    const distValEl = document.getElementById('tracking-dist-val');
    if (etaValEl) {
      etaValEl.textContent = data.etaMinutes ? `${data.etaMinutes} min` : (data.status === 'PENDING' ? 'Matching...' : 'Arrived');
    }
    if (distValEl) {
      distValEl.textContent = data.distanceKm ? `${data.distanceKm} km` : 'Calculating...';
    }

    // Provider Details Card
    const providerCard = document.getElementById('tracking-provider-card');
    if (providerCard) {
      if (data.providerId) {
        providerCard.style.display = 'block';
        providerCard.innerHTML = `
          <div class="glass-panel" style="padding:1.5rem; border:1px solid var(--border-teal); margin-bottom:1.5rem;">
            <div style="font-size:0.8rem; font-weight:700; color:var(--text-muted); text-transform:uppercase; margin-bottom:0.5rem;">Assigned Certified Rescue Unit</div>
            <div style="display:flex; justify-content:space-between; align-items:flex-start; flex-wrap:wrap; gap:1rem;">
              <div>
                <h3 style="font-size:1.3rem; font-weight:800; color:#fff;">${data.providerBusinessName || data.providerName}</h3>
                <div style="font-size:0.85rem; color:var(--text-secondary); margin-top:2px;">
                  Technician: <strong style="color:#fff;">${data.providerName}</strong> &bull; Vehicle: <span style="color:var(--teal-neon);">${data.providerVehiclePlate || 'Rapid Unit'}</span>
                </div>
              </div>
              <div style="display:flex; align-items:center; gap:0.75rem;">
                <div style="background:rgba(255, 183, 3, 0.15); border:1px solid var(--border-amber); color:var(--amber-warm); font-weight:800; font-size:0.95rem; padding:4px 10px; border-radius:6px; display:flex; align-items:center; gap:4px;">
                  <i data-lucide="star" style="fill:var(--amber-warm); width:14px; height:14px;"></i> ${data.providerRating || '4.9'}
                </div>
                <a href="tel:${data.providerPhone}" class="btn-primary btn-sm" style="text-decoration:none;">
                  <i data-lucide="phone"></i> Call
                </a>
              </div>
            </div>
          </div>
        `;
      } else {
        providerCard.innerHTML = `
          <div class="glass-panel" style="padding:1.5rem; text-align:center; border:1px dashed var(--border-teal); margin-bottom:1.5rem;">
            <div style="font-size:1rem; font-weight:700; color:var(--teal-neon); margin-bottom:0.4rem;">
              <span class="sos-pulse-ring" style="display:inline-block; vertical-align:middle; margin-right:6px;"></span>
              Broadcasting Request to Nearest Certified Units...
            </div>
            <div style="font-size:0.85rem; color:var(--text-secondary);">
              Your emergency signal is broadcasting across active response vehicles. The first available provider to accept will be locked in instantly.
            </div>
          </div>
        `;
      }
    }

    // Problem Photo Preview
    const photoBox = document.getElementById('tracking-photo-box');
    if (photoBox) {
      if (data.photoUrl) {
        photoBox.style.display = 'block';
        photoBox.innerHTML = `
          <div style="font-size:0.8rem; font-weight:700; color:var(--text-muted); text-transform:uppercase; margin-bottom:0.4rem;">Attached Problem Photo</div>
          <img src="${API.resolveUrl(data.photoUrl)}" alt="Vehicle issue" style="max-height:160px; border-radius:8px; border:1px solid var(--border-teal); cursor:pointer;" onclick="App.openLightbox('${API.resolveUrl(data.photoUrl)}')" />
        `;
      } else {
        photoBox.style.display = 'none';
      }
    }

    // Revised Quote Approval Box
    const quoteBox = document.getElementById('tracking-revised-quote-box');
    if (quoteBox) {
      if (data.quoteStatus === 'REVISED_PENDING') {
        quoteBox.style.display = 'block';
        quoteBox.innerHTML = `
          <div class="glass-panel" style="padding:1.5rem; background:rgba(30, 25, 12, 0.95); border:1px solid var(--border-amber); margin-bottom:1.5rem;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:0.75rem;">
              <div style="font-weight:800; color:var(--amber-warm); font-size:1rem; display:flex; align-items:center; gap:0.5rem;">
                <i data-lucide="alert-triangle"></i> Revised Quote Submitted by Provider
              </div>
              <span style="font-size:1.3rem; font-weight:800; color:#fff;">₹${App.formatINR(data.revisedQuote)}</span>
            </div>
            <div style="font-size:0.88rem; color:var(--text-secondary); margin-bottom:1rem;">
              <strong>Diagnostic Reason:</strong> ${data.revisedQuoteReason || 'On-site parts inspection required adjustment'}
            </div>
            <div style="display:flex; gap:0.75rem;">
              <button class="btn-primary btn-sm" onclick="Tracking.respondToQuote(true)">
                <i data-lucide="check"></i> Approve Revised Quote (₹${App.formatINR(data.revisedQuote)})
              </button>
              <button class="btn-secondary btn-sm" onclick="Tracking.respondToQuote(false)" style="color:var(--hazard-red);">
                <i data-lucide="x"></i> Decline
              </button>
            </div>
          </div>
        `;
      } else {
        quoteBox.style.display = 'none';
      }
    }

    // Share link button
    const shareBtn = document.getElementById('tracking-share-btn');
    if (shareBtn) {
      shareBtn.onclick = () => this.shareTrackingLink(data);
    }

    // Cancel Button visibility (only before completion)
    const cancelBtn = document.getElementById('tracking-cancel-btn');
    if (cancelBtn) {
      cancelBtn.style.display = (data.status !== 'COMPLETED' && data.status !== 'CANCELLED') ? 'inline-flex' : 'none';
    }

    if (window.lucide) lucide.createIcons();
  },

  updateTimelineNodes(status) {
    const statuses = ['PENDING', 'ACCEPTED', 'ON_THE_WAY', 'ARRIVED', 'COMPLETED'];
    const currentIndex = statuses.indexOf(status);

    const progressBar = document.getElementById('tracking-progress-fill');
    if (progressBar) {
      const pct = Math.max(0, (currentIndex / (statuses.length - 1)) * 100);
      progressBar.style.width = `${pct}%`;
    }

    statuses.forEach((s, idx) => {
      const nodeEl = document.getElementById(`step-node-${s}`);
      if (!nodeEl) return;

      nodeEl.classList.remove('active', 'completed');
      if (idx === currentIndex) {
        nodeEl.classList.add('active');
      } else if (idx < currentIndex) {
        nodeEl.classList.add('completed');
      }
    });
  },

  updateMapVisualization(data) {
    if (!this.map) this.initMap();
    if (!this.map) return;

    // Customer Marker
    if (!this.customerMarker) {
      const customerIcon = L.divIcon({
        className: 'customer-beacon-icon',
        html: `
          <div style="position:relative;">
            <div style="width:28px; height:28px; background:var(--teal-neon); border:3px solid #fff; border-radius:50%; box-shadow:0 0 20px var(--teal-glow); display:flex; align-items:center; justify-content:center; color:#000; font-weight:800; font-size:12px;">SOS</div>
            <div class="radar-ping" style="width:40px; height:40px; top:-6px; left:-6px;"></div>
          </div>`,
        iconSize: [28, 28],
        iconAnchor: [14, 14]
      });
      this.customerMarker = L.marker([data.latitude, data.longitude], { icon: customerIcon })
        .addTo(this.map)
        .bindPopup(`<strong>Your Location (Breakdown)</strong><br>${data.address}`);
    } else {
      this.customerMarker.setLatLng([data.latitude, data.longitude]);
    }

    // Assigned Provider Marker & Route
    if (data.providerLatitude && data.providerLongitude) {
      const pLat = data.providerLatitude;
      const pLon = data.providerLongitude;

      if (!this.providerMarker) {
        const truckIcon = L.divIcon({
          className: 'provider-truck-icon',
          html: `
            <div style="width:34px; height:34px; background:var(--amber-warm); border:2px solid #000; border-radius:50%; display:flex; align-items:center; justify-content:center; box-shadow:0 0 15px var(--amber-glow); color:#000; font-weight:800; font-size:16px;">
              &#128666;
            </div>`,
          iconSize: [34, 34],
          iconAnchor: [17, 17]
        });

        this.providerMarker = L.marker([pLat, pLon], { icon: truckIcon })
          .addTo(this.map)
          .bindPopup(`<strong>${data.providerBusinessName || 'Rescue Unit'}</strong><br>Plate: ${data.providerVehiclePlate || 'HR-02-RR-101'}`);
      } else {
        this.providerMarker.setLatLng([pLat, pLon]);
      }

      // Draw polyline connecting provider and customer
      if (this.routePolyline) {
        this.map.removeLayer(this.routePolyline);
      }

      this.routePolyline = L.polyline([
        [pLat, pLon],
        [data.latitude, data.longitude]
      ], {
        color: '#00f5d4',
        weight: 4,
        dashArray: '8, 8',
        opacity: 0.85
      }).addTo(this.map);

      // Fit bounds to show both
      const bounds = L.latLngBounds([
        [pLat, pLon],
        [data.latitude, data.longitude]
      ]);
      this.map.fitBounds(bounds, { padding: [60, 60] });

      // Start animated simulated route movement
      if (data.status === 'ON_THE_WAY' || data.status === 'ACCEPTED') {
        this.startSimulatedMovement(pLat, pLon, data.latitude, data.longitude);
      }
    } else {
      this.map.setView([data.latitude, data.longitude], 14);
    }
  },

  startSimulatedMovement(startLat, startLon, endLat, endLon) {
    if (this.animationTimer) clearInterval(this.animationTimer);

    // Increment simulated movement every 1.5 seconds
    this.animationTimer = setInterval(() => {
      if (this.simulatedProgress < 0.95) {
        this.simulatedProgress += 0.04;
        const currentLat = startLat + (endLat - startLat) * this.simulatedProgress;
        const currentLon = startLon + (endLon - startLon) * this.simulatedProgress;

        if (this.providerMarker) {
          this.providerMarker.setLatLng([currentLat, currentLon]);
        }

        // Dynamically reduce ETA
        const etaEl = document.getElementById('tracking-eta-val');
        if (etaEl && this.latestData && this.latestData.etaMinutes) {
          const remainingMinutes = Math.max(1, Math.round(this.latestData.etaMinutes * (1 - this.simulatedProgress)));
          etaEl.textContent = `${remainingMinutes} min`;
        }
      }
    }, 1500);
  },

  async respondToQuote(approved) {
    if (!this.currentRequestId) return;
    try {
      const res = await API.post(`/api/requests/${this.currentRequestId}/quote/respond`, { approved });
      App.showToast(approved ? 'Revised quote approved!' : 'Revised quote declined.', approved ? 'success' : 'info');
      this.fetchStatus();
    } catch (err) {
      App.showToast('Failed to respond to quote: ' + err.message, 'error');
    }
  },

  async cancelCurrentRequest() {
    if (!this.currentRequestId) return;
    const reason = prompt('Please enter cancellation reason:', 'Assistance no longer needed');
    if (reason === null) return;

    try {
      await API.patch(`/api/requests/${this.currentRequestId}/status`, {
        status: 'CANCELLED',
        cancelReason: reason
      });
      App.showToast('Rescue request cancelled', 'info');
      this.fetchStatus();
    } catch (err) {
      App.showToast('Failed to cancel request: ' + err.message, 'error');
    }
  },

  shareTrackingLink(data) {
    const url = `${window.location.origin}/index.html?track=${data.trackingCode}`;
    const text = `RoadResQ Emergency Alert: Tracking live roadside rescue for ${data.customerName || 'customer'} (${data.serviceTypeDisplay}). Live Tracker: ${url}`;

    if (navigator.clipboard) {
      navigator.clipboard.writeText(url);
      App.showToast('Live tracking link copied to clipboard!', 'success');
    }

    // Open WhatsApp Web/App Share
    const waUrl = `https://api.whatsapp.com/send?text=${encodeURIComponent(text)}`;
    window.open(waUrl, '_blank');
  }
};

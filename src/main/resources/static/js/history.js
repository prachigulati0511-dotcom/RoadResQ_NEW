/**
 * RoadResQ History & Ratings Module
 * Displays customer past requests, itemized invoices, and 5-star review modal
 */

const History = {
  myRequests: [],
  selectedRatingStars: 5,
  selectedTags: new Set(),
  activeReviewRequestId: null,

  init() {
    this.bindEvents();
  },

  bindEvents() {
    // Star rating picker
    document.querySelectorAll('.star-rating-item').forEach(star => {
      star.addEventListener('click', () => {
        const rating = parseInt(star.dataset.star, 10);
        this.setStarRating(rating);
      });
    });

    // Compliment tags
    document.querySelectorAll('.compliment-tag').forEach(tag => {
      tag.addEventListener('click', () => {
        const val = tag.dataset.tag;
        if (this.selectedTags.has(val)) {
          this.selectedTags.delete(val);
          tag.classList.remove('active');
        } else {
          this.selectedTags.add(val);
          tag.classList.add('active');
        }
      });
    });
  },

  async loadHistory() {
    if (!Auth.currentUser || Auth.currentUser.role !== 'CUSTOMER') return;
    App.showLoading(true, 'Fetching service history...');

    try {
      this.myRequests = await API.get('/api/requests/my');
      this.renderHistoryList();
    } catch (err) {
      console.warn('Failed to load history:', err);
    } finally {
      App.showLoading(false);
    }
  },

  renderHistoryList() {
    const container = document.getElementById('customer-history-list');
    if (!container) return;

    if (this.myRequests.length === 0) {
      container.innerHTML = `
        <div class="glass-panel" style="padding:3rem; text-align:center; color:var(--text-muted);">
          <div style="font-size:2.5rem; margin-bottom:0.75rem;">&#128196;</div>
          <h3 style="font-size:1.2rem; color:#fff; font-weight:700;">No Past Service Requests</h3>
          <p style="font-size:0.85rem; margin-top:0.3rem;">When you dispatch emergency assistance, your complete rescue records and itemized invoices will appear here.</p>
        </div>
      `;
      return;
    }

    container.innerHTML = this.myRequests.map(r => {
      const isCompleted = r.status === 'COMPLETED';
      const isCancelled = r.status === 'CANCELLED';
      const statusClass = isCompleted ? 'role-customer' : (isCancelled ? 'role-provider' : 'role-customer');

      return `
        <div class="glass-panel" style="padding:1.5rem; margin-bottom:1.25rem;">
          <div style="display:flex; justify-content:space-between; align-items:flex-start; flex-wrap:wrap; gap:1rem; margin-bottom:1rem;">
            <div>
              <div style="display:flex; align-items:center; gap:0.5rem; margin-bottom:0.3rem;">
                <span class="role-tag ${statusClass}">${r.status.replace(/_/g, ' ')}</span>
                <span style="font-size:0.8rem; color:var(--text-muted); font-family:monospace;">${r.trackingCode}</span>
                ${r.createdAt ? `<span style="font-size:0.75rem; color:var(--text-muted); margin-left:0.4rem;">&bull; ${App.formatISTDate(r.createdAt)}</span>` : ''}
              </div>
              <h3 style="font-size:1.25rem; font-weight:800; color:#fff;">${r.serviceTypeDisplay}</h3>
              <div style="font-size:0.85rem; color:var(--text-secondary); margin-top:2px;">
                Vehicle: <strong style="color:#fff;">${r.vehicleInfo || 'Vehicle'}</strong> &bull; Address: ${r.address}
              </div>
            </div>
            <div style="text-align:right;">
              <div style="font-size:0.75rem; color:var(--text-muted);">Total Paid / Invoiced</div>
              <div style="font-size:1.4rem; font-weight:800; color:var(--teal-neon);">₹${App.formatINR(r.totalEstimatedCost)}</div>
            </div>
          </div>

          <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:0.75rem; border-top:1px solid var(--border-subtle); padding-top:1rem; font-size:0.85rem;">
            <div style="color:var(--text-secondary);">
              Provider: <strong style="color:#fff;">${r.providerBusinessName || r.providerName || 'Certified Fleet'}</strong>
              ${r.providerPhone ? ` &bull; Tel: ${r.providerPhone}` : ''}
            </div>
            <div style="display:flex; gap:0.5rem;">
              <button class="btn-secondary btn-sm" onclick="Tracking.trackByCode('${r.trackingCode}')">
                <i data-lucide="eye"></i> View Live Tracker
              </button>
              ${isCompleted ? `
                <button class="btn-primary btn-sm" onclick="History.openReviewModal(${r.id})">
                  <i data-lucide="star"></i> Rate Service
                </button>
              ` : ''}
            </div>
          </div>
        </div>
      `;
    }).join('');

    if (window.lucide) lucide.createIcons();
  },

  openReviewModal(requestId) {
    this.activeReviewRequestId = requestId;
    this.selectedRatingStars = 5;
    this.selectedTags.clear();
    this.setStarRating(5);

    document.querySelectorAll('.compliment-tag').forEach(t => t.classList.remove('active'));
    const commentEl = document.getElementById('review-comment-input');
    if (commentEl) commentEl.value = '';

    App.openModal('review-modal');
  },

  setStarRating(stars) {
    this.selectedRatingStars = stars;
    document.querySelectorAll('.star-rating-item').forEach(star => {
      const s = parseInt(star.dataset.star, 10);
      if (s <= stars) {
        star.style.color = 'var(--amber-warm)';
        star.style.fill = 'var(--amber-warm)';
      } else {
        star.style.color = 'var(--text-muted)';
        star.style.fill = 'none';
      }
    });
  },

  async submitReview() {
    if (!this.activeReviewRequestId) return;

    const comment = document.getElementById('review-comment-input')?.value || '';
    const tags = Array.from(this.selectedTags).join(', ');

    App.showLoading(true, 'Submitting customer rating & review...');

    try {
      await API.post('/api/reviews', {
        requestId: this.activeReviewRequestId,
        stars: this.selectedRatingStars,
        tags,
        comment
      });

      App.playChime(880, 0.2);
      App.showToast('Thank you for rating your rescue technician!', 'success');
      App.closeModal('review-modal');
      this.loadHistory();
    } catch (err) {
      App.showToast('Review submission notice: ' + err.message, 'warning');
      App.closeModal('review-modal');
    } finally {
      App.showLoading(false);
    }
  }
};

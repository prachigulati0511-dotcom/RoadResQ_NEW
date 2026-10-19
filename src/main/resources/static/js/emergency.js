/**
 * RoadResQ Emergency Request & Problem Reporting Module
 * Handles camera capture, photo upload, interactive location picking,
 * transparent cost calculation, trusted contacts consent, and rescue dispatch
 */

const Emergency = {
  currentLocation: {
    lat: 30.1290,
    lon: 77.2674,
    address: 'Model Town, Jagadhri Road, Yamunanagar, Haryana 135001, India'
  },
  selectedService: 'FLAT_TYRE',
  selectedSeverity: 'NORMAL',
  uploadedPhotoUrl: null,
  contacts: [],
  miniMap: null,
  miniMarker: null,
  cameraStream: null,

  selectCity(cityName, lat, lon) {
    this.currentLocation.lat = lat;
    this.currentLocation.lon = lon;
    this.currentLocation.address = `${cityName}, Haryana, India`;
    const addrInput = document.getElementById('emergency-address-input');
    if (addrInput) addrInput.value = this.currentLocation.address;

    document.querySelectorAll('.btn-city-chip').forEach(btn => {
      btn.classList.toggle('active', btn.dataset.city === cityName);
    });

    if (this.miniMap && this.miniMarker) {
      this.miniMap.setView([lat, lon], 14);
      this.miniMarker.setLatLng([lat, lon]);
    }
    this.updateCostEstimate();
    App.showToast(`Location focused: ${cityName}`, 'info');
  },

  init() {
    this.bindEvents();
    this.initMiniMap();
    this.detectLocation();
    this.loadContacts();
    this.updateCostEstimate();
  },

  bindEvents() {
    // Service selection buttons
    document.querySelectorAll('.service-select-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        document.querySelectorAll('.service-select-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        this.selectedService = btn.dataset.service;
        this.updateCostEstimate();
      });
    });

    // Severity selection buttons
    document.querySelectorAll('.severity-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        document.querySelectorAll('.severity-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        this.selectedSeverity = btn.dataset.severity;
        this.updateCostEstimate();
      });
    });

    // File input drag & drop / picker
    const fileInput = document.getElementById('problem-photo-file');
    if (fileInput) {
      fileInput.addEventListener('change', (e) => {
        if (e.target.files && e.target.files[0]) {
          this.uploadFile(e.target.files[0]);
        }
      });
    }

    // Vehicle select change
    const vehSelect = document.getElementById('emergency-vehicle-select');
    if (vehSelect) {
      vehSelect.addEventListener('change', () => {
        if (vehSelect.value === 'NEW') {
          App.openModal('vehicle-modal');
        }
      });
    }
  },

  initMiniMap() {
    const mapEl = document.getElementById('emergency-mini-map');
    if (!mapEl || this.miniMap) return;

    if (typeof L === 'undefined') {
      console.warn('Leaflet library loading in progress...');
      return;
    }

    this.miniMap = L.map('emergency-mini-map', {
      center: [this.currentLocation.lat, this.currentLocation.lon],
      zoom: 14,
      zoomControl: true
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors',
      maxZoom: 19
    }).addTo(this.miniMap);

    const pinIcon = L.divIcon({
      className: 'custom-pin-beacon',
      html: `<div style="width:24px; height:24px; background:var(--teal-neon); border:3px solid #fff; border-radius:50%; box-shadow:0 0 15px var(--teal-glow);"></div>`,
      iconSize: [24, 24],
      iconAnchor: [12, 12]
    });

    this.miniMarker = L.marker([this.currentLocation.lat, this.currentLocation.lon], {
      draggable: true,
      icon: pinIcon
    }).addTo(this.miniMap);

    this.miniMarker.on('dragend', (e) => {
      const pos = e.target.getLatLng();
      this.currentLocation.lat = Math.round(pos.lat * 10000) / 10000;
      this.currentLocation.lon = Math.round(pos.lng * 10000) / 10000;
      this.reverseGeocode(this.currentLocation.lat, this.currentLocation.lon);
      this.updateCostEstimate();
    });
  },

  detectLocation() {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          this.currentLocation.lat = pos.coords.latitude;
          this.currentLocation.lon = pos.coords.longitude;
          if (this.miniMap && this.miniMarker) {
            this.miniMap.setView([this.currentLocation.lat, this.currentLocation.lon], 15);
            this.miniMarker.setLatLng([this.currentLocation.lat, this.currentLocation.lon]);
          }
          this.reverseGeocode(this.currentLocation.lat, this.currentLocation.lon);
          this.updateCostEstimate();
          App.showToast('GPS coordinates locked successfully!', 'success');
        },
        (err) => {
          console.warn('Geolocation unavailable or denied, using default city center coords');
        },
        { enableHighAccuracy: true, timeout: 5000 }
      );
    }
  },

  async reverseGeocode(lat, lon) {
    const addressInput = document.getElementById('emergency-address-input');
    if (!addressInput) return;

    try {
      const res = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}`);
      const data = await res.json();
      if (data && data.display_name) {
        this.currentLocation.address = data.display_name;
        addressInput.value = data.display_name;
      }
    } catch (e) {
      addressInput.value = `GPS: ${lat.toFixed(4)}, ${lon.toFixed(4)}`;
    }
  },

  // ---------------- CAMERA CAPTURE ----------------
  async startLiveCamera() {
    const videoEl = document.getElementById('camera-feed');
    const container = document.getElementById('live-camera-view');
    const uploadPrompt = document.getElementById('photo-dropzone-prompt');

    if (!videoEl || !container) return;

    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'environment', width: { ideal: 1280 } },
        audio: false
      });
      this.cameraStream = stream;
      videoEl.srcObject = stream;
      videoEl.play();

      if (uploadPrompt) uploadPrompt.style.display = 'none';
      container.style.display = 'block';
    } catch (err) {
      App.showToast('Camera access denied or unavailable. You can upload an image file instead.', 'warning');
      const fileInput = document.getElementById('problem-photo-file');
      if (fileInput) fileInput.click();
    }
  },

  snapPhoto() {
    const videoEl = document.getElementById('camera-feed');
    const canvas = document.createElement('canvas');
    if (!videoEl) return;

    canvas.width = videoEl.videoWidth || 640;
    canvas.height = videoEl.videoHeight || 480;
    const ctx = canvas.getContext('2d');
    ctx.drawImage(videoEl, 0, 0, canvas.width, canvas.height);

    const base64Data = canvas.toDataURL('image/jpeg', 0.85);

    // Stop camera stream
    this.stopLiveCamera();

    // Upload snapshot to backend
    this.uploadBase64(base64Data);
  },

  stopLiveCamera() {
    if (this.cameraStream) {
      this.cameraStream.getTracks().forEach(track => track.stop());
      this.cameraStream = null;
    }
    const container = document.getElementById('live-camera-view');
    if (container) container.style.display = 'none';
  },

  async uploadFile(file) {
    App.showLoading(true, 'Uploading and inspecting problem photo...');
    try {
      const formData = new FormData();
      formData.append('file', file);
      const res = await API.uploadFormData('/api/upload/problem-photo', formData);
      this.uploadedPhotoUrl = res.photoUrl;
      this.renderPhotoPreview(res.photoUrl);
      App.showToast('Photo uploaded and verified successfully!', 'success');
    } catch (err) {
      App.showToast(err.message || 'Failed to upload photo', 'error');
    } finally {
      App.showLoading(false);
    }
  },

  async uploadBase64(base64Data) {
    App.showLoading(true, 'Storing camera snapshot...');
    try {
      const res = await API.post('/api/upload/camera-photo', { image: base64Data });
      this.uploadedPhotoUrl = res.photoUrl;
      this.renderPhotoPreview(res.photoUrl);
      App.showToast('Camera snapshot uploaded successfully!', 'success');
    } catch (err) {
      App.showToast(err.message || 'Failed to store snapshot', 'error');
    } finally {
      App.showLoading(false);
    }
  },

  renderPhotoPreview(url) {
    const container = document.getElementById('photo-preview-wrapper');
    const dropzone = document.getElementById('photo-dropzone-prompt');

    if (dropzone) dropzone.style.display = 'none';
    if (container) {
      container.style.display = 'block';
      container.innerHTML = `
        <div style="position:relative; display:inline-block; border-radius:12px; overflow:hidden; border:2px solid var(--border-teal); box-shadow:var(--shadow-teal);">
          <img src="${API.resolveUrl(url)}" alt="Problem photo" style="max-height:220px; max-width:100%; display:block;" />
          <button type="button" onclick="Emergency.removePhoto()" style="position:absolute; top:8px; right:8px; background:rgba(0,0,0,0.7); color:#fff; border:none; border-radius:50%; width:28px; height:28px; cursor:pointer; display:flex; align-items:center; justify-content:center;">
            &times;
          </button>
        </div>
        <div style="font-size:0.8rem; color:var(--teal-neon); font-weight:700; margin-top:0.4rem;">
          <i data-lucide="check-circle" style="width:14px; height:14px; vertical-align:middle;"></i> Ready to send to assigned rescue provider
        </div>
      `;
      if (window.lucide) lucide.createIcons();
    }
  },

  removePhoto() {
    this.uploadedPhotoUrl = null;
    const container = document.getElementById('photo-preview-wrapper');
    const dropzone = document.getElementById('photo-dropzone-prompt');
    if (container) container.style.display = 'none';
    if (dropzone) dropzone.style.display = 'block';
  },

  // ---------------- TRANSPARENT COST ESTIMATOR ----------------
  async updateCostEstimate() {
    try {
      const estimate = await API.post('/api/cost-estimate', {
        serviceType: this.selectedService,
        severity: this.selectedSeverity,
        customerLat: this.currentLocation.lat,
        customerLon: this.currentLocation.lon
      });

      this.renderCostCard(estimate);
    } catch (e) {
      console.warn('Cost estimator sync failed:', e);
    }
  },

  renderCostCard(est) {
    const el = document.getElementById('cost-estimator-container');
    if (!el) return;

    el.innerHTML = `
      <div class="glass-panel" style="padding:1.5rem; background:rgba(12, 23, 38, 0.95); border:1px solid var(--border-teal);">
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:1rem;">
          <div style="font-weight:800; font-size:1.1rem; color:#fff; display:flex; align-items:center; gap:0.5rem;">
            <i data-lucide="receipt" style="color:var(--teal-neon);"></i> Transparent Cost Breakdown
          </div>
          <span style="font-size:0.75rem; background:var(--amber-dim); color:var(--amber-warm); border:1px solid var(--border-amber); padding:2px 8px; border-radius:9999px; font-weight:800;">DEMO PRICING</span>
        </div>

        <table class="cost-breakdown-table">
          <tr>
            <td>Base Rescue Fee (${this.selectedService.replace('_', ' ')})</td>
            <td>₹${App.formatINR(est.baseServiceFee)}</td>
          </tr>
          <tr>
            <td>Travel Fee (~${est.travelDistanceKm} km dispatch radius)</td>
            <td>₹${App.formatINR(est.travelFee)}</td>
          </tr>
          ${est.hazardSurcharge > 0 ? `
          <tr style="color:var(--hazard-red);">
            <td>Highway Hazard / Critical Urgency Surcharge</td>
            <td>₹${App.formatINR(est.hazardSurcharge)}</td>
          </tr>` : ''}
          <tr>
            <td>Subtotal</td>
            <td>₹${App.formatINR(est.subtotal)}</td>
          </tr>
          <tr>
            <td>Goods &amp; Services Tax (GST 18%)</td>
            <td>₹${App.formatINR(est.estimatedTax)}</td>
          </tr>
          <tr class="total-row">
            <td>Estimated Total (INR)</td>
            <td>₹${App.formatINR(est.totalEstimatedCost)}</td>
          </tr>
        </table>

        <div style="font-size:0.75rem; color:var(--text-muted); line-height:1.4; border-top:1px solid var(--border-subtle); padding-top:0.75rem;">
          ${est.pricingNotice}
        </div>
      </div>
    `;
    if (window.lucide) lucide.createIcons();
  },

  // ---------------- TRUSTED EMERGENCY CONTACTS ----------------
  async loadContacts() {
    if (!Auth.currentUser || Auth.currentUser.role !== 'CUSTOMER') return;
    try {
      this.contacts = await API.get('/api/contacts');
      this.renderContactsList();
    } catch (e) {
      console.warn('Failed to load contacts');
    }
  },

  renderContactsList() {
    const listEl = document.getElementById('trusted-contacts-list');
    if (!listEl) return;

    if (this.contacts.length === 0) {
      listEl.innerHTML = `<div style="font-size:0.85rem; color:var(--text-muted);">No contacts saved yet. Add a spouse, family member, or friend.</div>`;
      return;
    }

    listEl.innerHTML = this.contacts.map(c => `
      <div style="display:flex; justify-content:space-between; align-items:center; background:rgba(255,255,255,0.03); padding:0.6rem 0.9rem; border-radius:8px; margin-bottom:0.4rem; font-size:0.85rem;">
        <div>
          <strong style="color:#fff;">${c.name}</strong> <span style="color:var(--text-muted);">(${c.relationship})</span>
          <div style="color:var(--text-secondary); font-size:0.75rem;">${c.phone}</div>
        </div>
        <span style="font-size:0.72rem; color:var(--teal-neon); background:var(--teal-dim); padding:2px 8px; border-radius:4px; font-weight:700;">Alert Ready</span>
      </div>
    `).join('');
  },

  async addContact(data) {
    if (data.phone && !Auth.validateIndianPhone(data.phone)) {
      App.showToast('Please enter a valid 10-digit Indian phone number (+91 98XXX XXXXX)', 'warning');
      return;
    }
    try {
      await API.post('/api/contacts', data);
      App.showToast(`Contact ${data.name} saved!`, 'success');
      App.closeModal('contact-modal');
      await this.loadContacts();
    } catch (err) {
      App.showToast(err.message || 'Failed to save contact', 'error');
    }
  },

  populateVehicleDropdown() {
    const select = document.getElementById('emergency-vehicle-select');
    if (!select) return;

    const vehicles = Auth.userVehicles || [];
    select.innerHTML = `
      <option value="">-- Select Saved Vehicle --</option>
      ${vehicles.map(v => `
        <option value="${v.year} ${v.make} ${v.model} (${v.licensePlate})">
          ${v.year} ${v.make} ${v.model} - [${v.licensePlate}] (${v.fuelType})
        </option>
      `).join('')}
      <option value="NEW">+ Add New Vehicle...</option>
    `;
  },

  prefillFromDiagnosis(diag) {
    if (!diag) return;

    this.selectedService = diag.recommendedService;
    this.selectedSeverity = diag.severity;

    // Update active service button
    document.querySelectorAll('.service-select-btn').forEach(btn => {
      btn.classList.toggle('active', btn.dataset.service === diag.recommendedService);
    });

    // Update severity button
    document.querySelectorAll('.severity-btn').forEach(btn => {
      btn.classList.toggle('active', btn.dataset.severity === diag.severity);
    });

    // Populate problem description
    const descEl = document.getElementById('emergency-problem-desc');
    if (descEl) {
      descEl.value = `[AI Diagnostic Report]: ${diag.diagnosisTitle}\nProbable Causes:\n${diag.probableCauses.map(c => '- ' + c).join('\n')}\nNotes: ${diag.immediateActionAdvice}`;
    }

    this.updateCostEstimate();
  },

  // ---------------- SUBMIT DISPATCH REQUEST ----------------
  async submitRequest() {
    if (!Auth.currentUser) {
      App.showToast('Please log in or register before dispatching rescue', 'warning');
      App.openModal('auth-modal');
      return;
    }

    const address = document.getElementById('emergency-address-input')?.value || this.currentLocation.address;
    const vehicle = document.getElementById('emergency-vehicle-select')?.value || 'Customer Vehicle';
    const description = document.getElementById('emergency-problem-desc')?.value || '';
    const consentContacts = document.getElementById('emergency-consent-contacts')?.checked || false;

    const payload = {
      serviceType: this.selectedService,
      severity: this.selectedSeverity,
      latitude: this.currentLocation.lat,
      longitude: this.currentLocation.lon,
      address,
      vehicleInfo: vehicle,
      problemDescription: description,
      photoUrl: this.uploadedPhotoUrl,
      consentContacts
    };

    App.showLoading(true, 'Broadcasting emergency request to nearest rescue fleet...');

    try {
      const created = await API.post('/api/requests', payload);

      // If customer requested contact alerts, dispatch notification
      if (consentContacts) {
        try {
          await API.post(`/api/contacts/dispatch/${created.id}`);
        } catch (e) {
          console.warn('Contacts alert dispatch notice:', e);
        }
      }

      App.playChime(523, 0.2);
      App.showToast(`Rescue dispatched! Tracking code: ${created.trackingCode}`, 'success');

      // Navigate directly to tracking view
      App.navigateTo('tracking');
      Tracking.startTracking(created.id, created.trackingCode);
    } catch (err) {
      App.showToast('Failed to dispatch request: ' + err.message, 'error');
    } finally {
      App.showLoading(false);
    }
  }
};

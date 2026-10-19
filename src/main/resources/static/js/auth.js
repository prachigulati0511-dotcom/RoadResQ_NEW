/**
 * RoadResQ Auth Module
 * Manages user authentication, session state, vehicle list, and quick demo role switching
 */

const Auth = {
  currentUser: null,
  userVehicles: [],

  init() {
    this.checkSession();
  },

  async checkSession() {
    const token = API.getToken();
    if (!token) {
      // Auto-login default demo customer for smooth zero-friction instant demo experience
      await this.quickLogin('customer@roadresq.com', 'password123');
      return;
    }

    try {
      const user = await API.get('/api/auth/me');
      this.currentUser = user;
      this.updateUI();
      if (user.role === 'CUSTOMER') {
        this.loadVehicles();
      }
    } catch (err) {
      console.warn('Session expired, auto-logging in default customer');
      await this.quickLogin('customer@roadresq.com', 'password123');
    }
  },

  async login(email, password) {
    try {
      const res = await API.post('/api/auth/login', { email, password });
      API.setToken(res.token);
      this.currentUser = {
        id: res.id,
        name: res.name,
        email: res.email,
        phone: res.phone,
        role: res.role,
        profile: res.profile
      };
      this.updateUI();
      App.showToast(`Welcome back, ${res.name}! (${res.role})`, 'success');
      App.playChime(660, 0.1);

      if (res.role === 'PROVIDER') {
        App.navigateTo('provider');
        Provider.loadIncomingRequests();
      } else {
        this.loadVehicles();
        Emergency.loadContacts();
      }
      return res;
    } catch (err) {
      App.showToast(err.message || 'Login failed', 'error');
      throw err;
    }
  },

  validateIndianPhone(phone) {
    if (!phone) return false;
    const cleaned = phone.replace(/[\s\-\(\)]/g, '');
    return /^(\+91)?[6789]\d{9}$/.test(cleaned);
  },

  async register(data) {
    if (data.phone && !this.validateIndianPhone(data.phone)) {
      App.showToast('Please enter a valid 10-digit Indian mobile number (+91 98XXX XXXXX)', 'warning');
      return;
    }
    try {
      const res = await API.post('/api/auth/register', data);
      API.setToken(res.token);
      this.currentUser = {
        id: res.id,
        name: res.name,
        email: res.email,
        phone: res.phone,
        role: res.role,
        profile: res.profile
      };
      this.updateUI();
      App.showToast(`Account created! Welcome to RoadResQ, ${res.name}`, 'success');

      if (res.role === 'PROVIDER') {
        App.navigateTo('provider');
      } else {
        App.navigateTo('home');
      }
      return res;
    } catch (err) {
      App.showToast(err.message || 'Registration failed', 'error');
      throw err;
    }
  },

  logout() {
    API.setToken(null);
    this.currentUser = null;
    this.updateUI();
    App.showToast('You have been logged out', 'info');
    App.navigateTo('home');
  },

  async quickLogin(email, password) {
    try {
      await this.login(email, password);
    } catch (e) {
      console.error('Quick login failed:', e);
    }
  },

  async loadVehicles() {
    if (!this.currentUser || this.currentUser.role !== 'CUSTOMER') return;
    try {
      this.userVehicles = await API.get('/api/auth/vehicles');
      this.renderVehiclesList();
      Emergency.populateVehicleDropdown();
    } catch (err) {
      console.error('Failed to load vehicles:', err);
    }
  },

  async addVehicle(data) {
    try {
      const saved = await API.post('/api/auth/vehicles', data);
      App.showToast(`Vehicle ${saved.make} ${saved.model} saved!`, 'success');
      await this.loadVehicles();
      return saved;
    } catch (err) {
      App.showToast(err.message || 'Failed to add vehicle', 'error');
      throw err;
    }
  },

  async deleteVehicle(id) {
    try {
      await API.delete(`/api/auth/vehicles/${id}`);
      App.showToast('Vehicle removed', 'info');
      await this.loadVehicles();
    } catch (err) {
      App.showToast('Failed to delete vehicle', 'error');
    }
  },

  updateUI() {
    const userChip = document.getElementById('nav-user-chip');
    const userNameEl = document.getElementById('nav-user-name');
    const roleTagEl = document.getElementById('nav-role-tag');
    const providerNav = document.getElementById('nav-provider-link');
    const myRequestsNav = document.getElementById('nav-history-link');

    if (!userChip) return;

    if (this.currentUser) {
      userChip.style.display = 'flex';
      userNameEl.textContent = this.currentUser.name;
      roleTagEl.textContent = this.currentUser.role;
      roleTagEl.className = `role-tag ${this.currentUser.role === 'PROVIDER' ? 'role-provider' : 'role-customer'}`;

      if (this.currentUser.role === 'PROVIDER') {
        if (providerNav) providerNav.style.display = 'flex';
        if (myRequestsNav) myRequestsNav.style.display = 'none';
      } else {
        if (providerNav) providerNav.style.display = 'none';
        if (myRequestsNav) myRequestsNav.style.display = 'flex';
      }
    } else {
      userChip.style.display = 'none';
    }
  },

  renderVehiclesList() {
    const container = document.getElementById('saved-vehicles-list');
    if (!container) return;

    if (!this.userVehicles || this.userVehicles.length === 0) {
      container.innerHTML = `<div class="empty-state-text" style="color:var(--text-muted); padding:1rem 0;">No vehicles saved yet. Add your vehicle for 1-click rescue dispatch.</div>`;
      return;
    }

    container.innerHTML = this.userVehicles.map(v => `
      <div class="glass-panel" style="padding:1rem; margin-bottom:0.75rem; display:flex; justify-content:space-between; align-items:center;">
        <div>
          <div style="font-weight:700; color:#fff; font-size:0.95rem;">
            ${v.year} ${v.make} ${v.model}
            <span style="font-size:0.75rem; color:var(--teal-neon); margin-left:0.5rem; background:var(--teal-dim); padding:2px 8px; border-radius:4px;">${v.fuelType}</span>
          </div>
          <div style="font-size:0.8rem; color:var(--text-secondary); margin-top:2px;">
            Plate: <strong style="color:#fff;">${v.licensePlate}</strong> &bull; Type: ${v.vehicleType} ${v.color ? '&bull; Color: ' + v.color : ''}
          </div>
        </div>
        <button class="btn-secondary btn-sm" onclick="Auth.deleteVehicle(${v.id})" title="Delete Vehicle" style="padding:4px 8px; color:var(--hazard-red);">
          <i data-lucide="trash-2"></i>
        </button>
      </div>
    `).join('');

    if (window.lucide) lucide.createIcons();
  }
};

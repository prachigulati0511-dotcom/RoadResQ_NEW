/**
 * RoadResQ Master Application Controller
 * Handles SPA navigation, audio synthesizer feedback, modal controls, and toast notifications
 */

const I18N = {
  en: {
    langLabel: 'हिंदी',
    brandSub: 'AI Roadside Platform • India',
    navHome: 'Home',
    navAssistant: 'AI Assistant',
    navSOS: 'Request SOS',
    navTracker: 'Live Tracker',
    navProvider: 'Provider Radar',
    navHistory: 'History',
    btnSOS: 'SOS DISPATCH',
    emergency112Title: 'National Emergency Helpline: Dial 112',
    emergency112Sub: 'Dial 112 for Police, Ambulance, or Fire. RoadResQ provides mechanical vehicle breakdown and roadside recovery.',
    heroTag: 'Next-Gen Roadside Telemetry • Yamunanagar & National Highways',
    heroHeading: 'Instant Road Rescue. <br /><span class="text-gradient">Powered by Real-Time AI.</span>',
    heroDesc: 'Stranded in Yamunanagar, Haryana or along National Highways? Our autonomous triage engine analyzes your vehicle symptoms and dispatches the nearest certified recovery unit in under 15 minutes.',
    btnEmergency: 'Dispatch Emergency Rescue',
    btnAssistant: 'Smart Breakdown Assistant',
    activeFleets: 'Active Fleets Online',
    avgArrival: 'Avg Arrival Time',
    dispatchReliability: 'Dispatch Reliability',
    servicesTitle: 'Emergency Rescue Fleet Capabilities',
    servicesSub: 'Certified Roadside Services (India)',
    wizardTitle: 'Smart Breakdown Assistant',
    wizardDesc: 'Answer 4 quick symptom questions. Our AI engine will infer probable causes, evaluate hazard severity, and prepare the exact rescue dispatch.',
    stepSounds: 'Sounds',
    stepVisuals: 'Visual & Odor',
    stepWarnings: 'Warning Lamps',
    stepMobility: 'Mobility',
    emergencyHeading: 'Request Emergency Assistance',
    emergencyDesc: 'Confirm your breakdown category, snap or upload a photo of the problem, and dispatch nearby response units with transparent upfront pricing in INR (₹).',
    baseService: 'Base Service',
    confirmDispatch: 'Confirm & Dispatch Rescue Fleet'
  },
  hi: {
    langLabel: 'English',
    brandSub: 'एआई सड़क सहायता • भारत',
    navHome: 'मुख्य पृष्ठ',
    navAssistant: 'एआई सहायक',
    navSOS: 'आपातकालीन सहायता',
    navTracker: 'लाइव ट्रैकर',
    navProvider: 'प्रदाता रडार',
    navHistory: 'सेवा इतिहास',
    btnSOS: 'आपातकालीन एसओएस',
    emergency112Title: 'राष्ट्रीय आपातकालीन हेल्पलाइन: 112 डायल करें',
    emergency112Sub: 'पुलिस, एम्बुलेंस या फायर के लिए 112 डायल करें। RoadResQ केवल यांत्रिक सड़क बचाव और टोइंग सहायता देता है।',
    heroTag: 'नेक्स्ट-जेन सड़क सहायता • यमुनानगर व राष्ट्रीय राजमार्ग',
    heroHeading: 'तत्काल सड़क बचाव। <br /><span class="text-gradient">रियल-टाइम एआई द्वारा संचालित।</span>',
    heroDesc: 'यमुनानगर (हरियाणा) या हाईवे पर वाहन खराब हो गया? हमारा एआई सिस्टम 15 मिनट में नजदीकी प्रमाणित मैकेनिक या टोइंग वैन भेजता है।',
    btnEmergency: 'आपातकालीन बचाव भेजें',
    btnAssistant: 'स्मार्ट ब्रेकडाउन सहायक',
    activeFleets: 'सक्रिय बचाव वाहन',
    avgArrival: 'औसत आगमन समय',
    dispatchReliability: 'बचाव विश्वसनीयता',
    servicesTitle: 'आपातकालीन बचाव सेवाएं',
    servicesSub: 'प्रमाणित सड़क सहायता सेवाएं (भारत)',
    wizardTitle: 'स्मार्ट ब्रेकडाउन सहायक',
    wizardDesc: '4 सरल प्रश्नों के उत्तर दें। हमारा एआई इंजन खराबी का कारण बताएगा और सही सेवा सुझाएगा।',
    stepSounds: 'आवाजें',
    stepVisuals: 'धुआं / गंध',
    stepWarnings: 'चेतावनी बत्तियां',
    stepMobility: 'गाड़ी की स्थिति',
    emergencyHeading: 'आपातकालीन सहायता अनुरोध',
    emergencyDesc: 'खराबी चुनें, समस्या की फोटो लें, और स्पष्ट भारतीय रुपयों (₹) के किराए पर नजदीकी बचाव वाहन बुलाएं।',
    baseService: 'शुरुआती शुल्क',
    confirmDispatch: 'पुष्टि करें और सहायता भेजें'
  }
};

const App = {
  currentView: 'home',
  currentLang: localStorage.getItem('roadresq_lang') || 'en',
  audioCtx: null,

  init() {
    this.initAudio();
    this.bindGlobalEvents();
    this.applyLanguage();

    // Initialize all modules
    Auth.init();
    Assistant.init();
    Emergency.init();
    Tracking.init();
    Provider.init();
    History.init();

    if (window.lucide) {
      lucide.createIcons();
    }
  },

  formatINR(amount) {
    if (amount == null || isNaN(amount)) return '0';
    return Number(amount).toLocaleString('en-IN', {
      maximumFractionDigits: 2,
      minimumFractionDigits: 0
    });
  },

  formatISTDate(dateStr) {
    if (!dateStr) return '';
    try {
      const d = new Date(dateStr);
      return d.toLocaleDateString('en-IN', {
        timeZone: 'Asia/Kolkata',
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        hour12: true
      });
    } catch (e) {
      return dateStr;
    }
  },

  toggleLanguage() {
    this.currentLang = this.currentLang === 'en' ? 'hi' : 'en';
    localStorage.setItem('roadresq_lang', this.currentLang);
    this.applyLanguage();
    this.showToast(this.currentLang === 'hi' ? 'भाषा बदलकर हिंदी कर दी गई' : 'Language switched to English', 'info');
  },

  applyLanguage() {
    const dict = I18N[this.currentLang] || I18N.en;
    document.querySelectorAll('[data-i18n]').forEach(el => {
      const key = el.dataset.i18n;
      if (dict[key]) {
        el.innerHTML = dict[key];
      }
    });

    const langToggleLabel = document.getElementById('current-lang-label');
    if (langToggleLabel) {
      langToggleLabel.textContent = dict.langLabel;
    }

    if (window.lucide) lucide.createIcons();
  },

  initAudio() {
    // Lazy initialize Web Audio API on first user gesture
    const unlockAudio = () => {
      if (!this.audioCtx) {
        const AudioContext = window.AudioContext || window.webkitAudioContext;
        if (AudioContext) {
          this.audioCtx = new AudioContext();
        }
      }
      document.removeEventListener('click', unlockAudio);
    };
    document.addEventListener('click', unlockAudio);
  },

  playChime(freq = 520, duration = 0.15) {
    try {
      if (!this.audioCtx) {
        const AudioContext = window.AudioContext || window.webkitAudioContext;
        if (AudioContext) this.audioCtx = new AudioContext();
      }
      if (!this.audioCtx) return;

      if (this.audioCtx.state === 'suspended') {
        this.audioCtx.resume();
      }

      const osc = this.audioCtx.createOscillator();
      const gain = this.audioCtx.createGain();

      osc.type = 'sine';
      osc.frequency.setValueAtTime(freq, this.audioCtx.currentTime);

      gain.gain.setValueAtTime(0.12, this.audioCtx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, this.audioCtx.currentTime + duration);

      osc.connect(gain);
      gain.connect(this.audioCtx.destination);

      osc.start();
      osc.stop(this.audioCtx.currentTime + duration);
    } catch (e) {
      // Audio autoplay policy or unavailable
    }
  },

  bindGlobalEvents() {
    // Navigation link clicks
    document.querySelectorAll('[data-navigate]').forEach(el => {
      el.addEventListener('click', (e) => {
        e.preventDefault();
        const targetView = el.dataset.navigate;
        this.navigateTo(targetView);
      });
    });

    // Close modal on backdrop click
    document.querySelectorAll('.modal-backdrop').forEach(modal => {
      modal.addEventListener('click', (e) => {
        if (e.target === modal) {
          this.closeModal(modal.id);
        }
      });
    });
  },

  navigateTo(viewName) {
    this.currentView = viewName;

    // Hide all view sections
    document.querySelectorAll('.view-section').forEach(sec => {
      sec.classList.remove('active');
    });

    // Show target section
    const target = document.getElementById(`view-${viewName}`);
    if (target) {
      target.classList.add('active');
    }

    // Update nav links active state
    document.querySelectorAll('.nav-link').forEach(link => {
      link.classList.toggle('active', link.dataset.navigate === viewName);
    });

    // Trigger module-specific refresh actions
    if (viewName === 'emergency') {
      Emergency.populateVehicleDropdown();
      setTimeout(() => {
        if (Emergency.miniMap) Emergency.miniMap.invalidateSize();
      }, 200);
    } else if (viewName === 'tracking') {
      setTimeout(() => {
        if (Tracking.map) Tracking.map.invalidateSize();
      }, 200);
    } else if (viewName === 'history') {
      History.loadHistory();
    } else if (viewName === 'provider') {
      Provider.loadIncomingRequests();
      Provider.loadMyActiveJobs();
    }

    window.scrollTo({ top: 0, behavior: 'smooth' });
    if (window.lucide) lucide.createIcons();
  },

  openModal(modalId) {
    const el = document.getElementById(modalId);
    if (el) {
      el.classList.add('active');
      if (window.lucide) lucide.createIcons();
    }
  },

  closeModal(modalId) {
    const el = document.getElementById(modalId);
    if (el) {
      el.classList.remove('active');
    }
  },

  openLightbox(imageUrl) {
    const imgEl = document.getElementById('lightbox-image');
    if (imgEl) {
      imgEl.src = imageUrl;
      this.openModal('lightbox-modal');
    }
  },

  showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;

    let iconName = 'info';
    if (type === 'success') iconName = 'check-circle';
    if (type === 'error') iconName = 'alert-triangle';
    if (type === 'warning') iconName = 'alert-circle';

    toast.innerHTML = `
      <i data-lucide="${iconName}" style="width:18px; height:18px; flex-shrink:0;"></i>
      <span style="flex:1;">${message}</span>
    `;

    container.appendChild(toast);
    if (window.lucide) lucide.createIcons();

    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateX(100%)';
      toast.style.transition = 'all 0.3s ease-out';
      setTimeout(() => toast.remove(), 300);
    }, 4500);
  },

  showLoading(show, message = 'Processing...') {
    let loader = document.getElementById('global-loader');
    if (!loader && show) {
      loader = document.createElement('div');
      loader.id = 'global-loader';
      loader.className = 'modal-backdrop active';
      loader.style.zIndex = '99999';
      loader.innerHTML = `
        <div style="background:var(--bg-surface); padding:2rem 3rem; border-radius:16px; border:1px solid var(--border-teal); text-align:center; box-shadow:var(--shadow-teal);">
          <div class="sos-pulse-ring" style="width:24px; height:24px; margin:0 auto 1.25rem auto; background:var(--teal-neon);"></div>
          <div id="loader-msg" style="font-weight:700; color:#fff; font-size:1.05rem;">${message}</div>
        </div>
      `;
      document.body.appendChild(loader);
    } else if (loader) {
      loader.classList.toggle('active', show);
      const msgEl = document.getElementById('loader-msg');
      if (msgEl) msgEl.textContent = message;
    }
  }
};

// Auto-boot when DOM ready
document.addEventListener('DOMContentLoaded', () => {
  App.init();
});

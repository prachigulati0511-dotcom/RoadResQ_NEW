/**
 * RoadResQ Smart Breakdown Assistant
 * Interactive multi-step diagnostic triage & safe rule-based expert recommendation engine
 */

const Assistant = {
  currentStep: 1,
  totalSteps: 4,
  answers: {
    sound: 'none',
    visual: 'none',
    warningLight: 'none',
    engineBehavior: 'normal',
    mobility: 'normal'
  },
  latestResult: null,

  init() {
    this.bindEvents();
    this.updateStepperUI();
  },

  bindEvents() {
    // Card selections
    document.querySelectorAll('.symptom-choice-card').forEach(card => {
      card.addEventListener('click', (e) => {
        const category = card.dataset.category;
        const value = card.dataset.value;

        // Deselect siblings in same container
        card.parentElement.querySelectorAll('.symptom-choice-card').forEach(c => c.classList.remove('selected'));
        card.classList.add('selected');

        this.answers[category] = value;
      });
    });
  },

  nextStep() {
    if (this.currentStep < this.totalSteps) {
      this.currentStep++;
      this.updateStepperUI();
      window.scrollTo({ top: 100, behavior: 'smooth' });
    } else {
      this.runDiagnosis();
    }
  },

  prevStep() {
    if (this.currentStep > 1) {
      this.currentStep--;
      this.updateStepperUI();
      window.scrollTo({ top: 100, behavior: 'smooth' });
    }
  },

  goToStep(step) {
    if (step >= 1 && step <= this.totalSteps) {
      this.currentStep = step;
      this.updateStepperUI();
    }
  },

  updateStepperUI() {
    // Update step wizard indicator
    document.querySelectorAll('.wizard-step-item').forEach((item, idx) => {
      const stepNum = idx + 1;
      item.classList.remove('active', 'completed');
      if (stepNum === this.currentStep) {
        item.classList.add('active');
      } else if (stepNum < this.currentStep) {
        item.classList.add('completed');
      }
    });

    // Show active step section
    for (let i = 1; i <= this.totalSteps; i++) {
      const stepEl = document.getElementById(`assistant-step-${i}`);
      if (stepEl) {
        stepEl.style.display = (i === this.currentStep) ? 'block' : 'none';
      }
    }

    // Hide result card while answering
    const resultCard = document.getElementById('assistant-result-section');
    if (resultCard) {
      resultCard.style.display = 'none';
    }

    // Update button text on last step
    const nextBtn = document.getElementById('assistant-next-btn');
    if (nextBtn) {
      if (this.currentStep === this.totalSteps) {
        nextBtn.innerHTML = `<span>Analyze Symptoms</span> <i data-lucide="sparkles"></i>`;
      } else {
        nextBtn.innerHTML = `<span>Next Question</span> <i data-lucide="arrow-right"></i>`;
      }
      if (window.lucide) lucide.createIcons();
    }

    const prevBtn = document.getElementById('assistant-prev-btn');
    if (prevBtn) {
      prevBtn.style.visibility = (this.currentStep === 1) ? 'hidden' : 'visible';
    }
  },

  async runDiagnosis() {
    App.showLoading(true, 'AI Diagnostic Engine analyzing vehicle symptoms...');

    try {
      const result = await API.post('/api/assistant/diagnose', this.answers);
      this.latestResult = result;
      this.renderDiagnosisResult(result);
      App.playChime(880, 0.15);
    } catch (err) {
      App.showToast('Diagnostic analysis error: ' + err.message, 'error');
    } finally {
      App.showLoading(false);
    }
  },

  renderDiagnosisResult(res) {
    const resultSection = document.getElementById('assistant-result-section');
    const questionsContainer = document.getElementById('assistant-questions-container');

    if (!resultSection) return;

    if (questionsContainer) questionsContainer.style.display = 'none';
    resultSection.style.display = 'block';

    const severityColor = res.severity === 'CRITICAL_HAZARD' ? 'var(--hazard-red)' :
                          res.severity === 'URGENT' ? 'var(--amber-warm)' : 'var(--teal-neon)';

    resultSection.innerHTML = `
      <div class="diagnostic-result-card glass-panel-glow">
        <div style="display:flex; justify-content:space-between; align-items:flex-start; flex-wrap:wrap; gap:1rem; margin-bottom:1.5rem;">
          <div>
            <div style="display:inline-flex; align-items:center; gap:0.5rem; background:rgba(0,245,212,0.12); color:var(--teal-neon); border:1px solid var(--border-teal); padding:4px 12px; border-radius:9999px; font-size:0.8rem; font-weight:800; margin-bottom:0.75rem;">
              <i data-lucide="cpu"></i> AI Expert Assessment Complete
            </div>
            <h2 style="font-size:1.8rem; font-weight:800; color:#fff; line-height:1.2;">${res.diagnosisTitle}</h2>
          </div>
          <div style="text-align:right;">
            <div style="font-size:0.8rem; color:var(--text-muted); font-weight:700; text-transform:uppercase;">Triage Severity</div>
            <span style="display:inline-block; margin-top:4px; padding:4px 12px; border-radius:6px; font-size:0.85rem; font-weight:800; background:${severityColor}22; color:${severityColor}; border:1px solid ${severityColor}66;">
              ${res.severity.replace('_', ' ')}
            </span>
          </div>
        </div>

        <!-- Confidence Meter -->
        <div style="background:rgba(255,255,255,0.03); border:1px solid var(--border-subtle); border-radius:12px; padding:1.2rem; margin-bottom:1.5rem;">
          <div style="display:flex; justify-content:space-between; align-items:center; font-size:0.88rem; font-weight:700; margin-bottom:0.5rem;">
            <span style="color:var(--text-secondary);">Diagnostic Confidence Score</span>
            <span style="color:var(--teal-neon); font-size:1.1rem; font-weight:800;">${res.confidencePercent}%</span>
          </div>
          <div class="meter-track">
            <div class="meter-fill" style="width: ${res.confidencePercent}%;"></div>
          </div>
        </div>

        <!-- Recommended Service Box -->
        <div class="glass-panel" style="background:var(--bg-surface-elevated); padding:1.5rem; border:1px solid var(--border-teal); margin-bottom:1.5rem;">
          <div style="font-size:0.8rem; font-weight:700; color:var(--text-muted); text-transform:uppercase; letter-spacing:0.05em; margin-bottom:0.3rem;">Recommended Rescue Service</div>
          <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:1rem;">
            <div>
              <div style="font-size:1.4rem; font-weight:800; color:#fff;">${res.recommendedServiceName}</div>
              <div style="font-size:0.85rem; color:var(--text-secondary); margin-top:2px;">${res.immediateActionAdvice}</div>
            </div>
            <div style="text-align:right;">
              <div style="font-size:0.75rem; color:var(--text-muted);">Estimated Base Service</div>
              <div style="font-size:1.5rem; font-weight:800; color:var(--teal-neon);">₹${App.formatINR(res.estimatedBaseCost)}</div>
            </div>
          </div>
        </div>

        <!-- Safety Protocols Banner -->
        <div style="background:rgba(239, 35, 60, 0.08); border-left:4px solid var(--hazard-red); padding:1.25rem; border-radius:0 12px 12px 0; margin-bottom:1.5rem;">
          <div style="font-weight:800; color:var(--hazard-red); font-size:0.95rem; display:flex; align-items:center; gap:0.5rem; margin-bottom:0.5rem;">
            <i data-lucide="shield-alert"></i> Critical Safety Protocols:
          </div>
          <ul style="list-style:none; padding-left:0; font-size:0.88rem; color:var(--text-secondary); display:flex; flex-direction:column; gap:0.4rem;">
            ${res.safetyProtocols.map(p => `
              <li style="display:flex; align-items:flex-start; gap:0.5rem;">
                <span style="color:var(--hazard-red); font-weight:bold;">&bull;</span>
                <span>${p}</span>
              </li>
            `).join('')}
          </ul>
        </div>

        <!-- Probable Causes -->
        <div style="margin-bottom:2rem;">
          <div style="font-weight:700; font-size:0.95rem; color:#fff; margin-bottom:0.75rem;">Likely Mechanical Causes:</div>
          <div style="display:grid; grid-template-columns:repeat(auto-fit, minmax(260px, 1fr)); gap:0.75rem;">
            ${res.probableCauses.map(cause => `
              <div style="background:rgba(255,255,255,0.03); border:1px solid var(--border-subtle); padding:0.9rem; border-radius:8px; font-size:0.85rem; color:var(--text-secondary); display:flex; align-items:center; gap:0.6rem;">
                <i data-lucide="alert-circle" style="color:var(--amber-warm); width:18px; height:18px; flex-shrink:0;"></i>
                <span>${cause}</span>
              </div>
            `).join('')}
          </div>
        </div>

        <!-- Action Buttons -->
        <div style="display:flex; gap:1rem; flex-wrap:wrap;">
          <button class="btn-primary" onclick="Assistant.transferToEmergencyRequest()" style="font-size:1.05rem; padding:0.9rem 2rem;">
            <i data-lucide="send"></i> Dispatch ${res.recommendedServiceName} Now
          </button>
          <button class="btn-secondary" onclick="Assistant.restartAssistant()">
            <i data-lucide="refresh-cw"></i> Start Over
          </button>
        </div>
      </div>
    `;

    if (window.lucide) lucide.createIcons();
    resultSection.scrollIntoView({ behavior: 'smooth' });
  },

  transferToEmergencyRequest() {
    if (!this.latestResult) return;

    // Navigate to request screen
    App.navigateTo('emergency');

    // Pre-fill Emergency Request Form with AI diagnosis
    Emergency.prefillFromDiagnosis(this.latestResult);
    App.showToast(`AI diagnostic profile transferred to Emergency Request!`, 'success');
  },

  restartAssistant() {
    this.currentStep = 1;
    this.latestResult = null;
    const questionsContainer = document.getElementById('assistant-questions-container');
    const resultSection = document.getElementById('assistant-result-section');

    if (questionsContainer) questionsContainer.style.display = 'block';
    if (resultSection) resultSection.style.display = 'none';

    this.updateStepperUI();
  }
};

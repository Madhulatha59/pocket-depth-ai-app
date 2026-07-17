/* ============================================================
   app.js — Core SPA Router, State Manager & 22+ Screen Views
   Replicates the exact layout, workflow, and user interface
   of the Android Pocket Depth AI app.
   ============================================================ */

// --- Global Service Fallbacks (Robustness Layer) ---
var SafeStorage = window.SafeStorage || (typeof SafeStorage !== 'undefined' ? SafeStorage : {
  memoryStore: {},
  getItem(key) { return this.memoryStore[key] || null; },
  setItem(key, value) { this.memoryStore[key] = value; },
  removeItem(key) { delete this.memoryStore[key]; }
});

var ApiService = window.ApiService || (typeof ApiService !== 'undefined' ? ApiService : {
  isFirebaseConnected() { return false; },
  reinitializeFirebase() { return false; },
  predictDisease() { return Promise.resolve({ success: false }); },
  getPatients() { return Promise.resolve([]); },
  saveToothData() { return Promise.resolve({ success: false }); }
});

var AnalyticsEngine = window.AnalyticsEngine || (typeof AnalyticsEngine !== 'undefined' ? AnalyticsEngine : {
  computeMetrics() { return { meanPpd: 2.0, meanCal: 0.5, bopPercentage: 10, deepPocketsCount: 0, clinicalVerdict: 'Healthy Periodontium', stageAndGrade: 'Healthy, Grade A', probHealthy: 90, probModerate: 8, probSevere: 2 }; }
});

var VoiceService = window.VoiceService || (typeof VoiceService !== 'undefined' ? VoiceService : {
  isSupported() { return false; },
  start() {},
  stop() {}
});

var Parser = window.Parser || (typeof Parser !== 'undefined' ? Parser : {
  parseSpeech() { return null; }
});

// --- Central Application State ---

const AppState = {
  // Authentication & Profile State
  clinicalUserName: 'Sarah Johnson',
  clinicalUserEmail: 'sarah.johnson@clinical.ai',
  clinicalUserPhone: '555-0199',
  clinicalUserLicense: 'LIC-2026-9874',
  clinicalUserClinic: 'Apex Dental Care & Periodontics',
  isAuthenticated: false,

  // Patient Directory State
  patients: [],
  activePatientName: 'Sarah Johnson',
  
  // Dashboard & Filter States
  notifications: [
    { id: 1, title: 'AI Baseline Synced', desc: 'Successfully synchronized clinical decision metrics locally.', time: '10m ago', icon: '🧠', bg: '#E8EBFF', color: '#5D48D1' },
    { id: 2, title: 'Database Sync Completed', desc: 'Periodontal cases backed up to Cloud database', time: '2h ago', icon: '☁️', bg: '#E8F5E9', color: '#4CAF50' },
    { id: 3, title: 'Deep Pockets Flagged', desc: 'Patient Sarah Johnson shows 14 sites >= 4mm', time: '1d ago', icon: '⚠️', bg: '#FFF3E0', color: '#FF9800' }
  ],
  dashboardFilter: 'All Cases',

  // Current Examination Probing Measurements (1 to 32 Teeth)
  // Mapping of "tooth{number}_ppd_{index}" and "tooth{number}_cal_{index}"
  manualPpd: {},
  manualCal: {},
  manualBop: {}, // Mapping of tooth number (1-32) -> Boolean

  // Dynamic Clinical Metrics & AI Verdicts
  meanPpd: 4.2,
  meanCal: 3.1,
  bopPercentage: 45,
  deepPocketsCount: 14,
  clinicalVerdict: 'Moderate Periodontitis',
  stageAndGrade: 'Stage II, Grade B',
  probHealthy: 8,
  probModerate: 78,
  probSevere: 14,

  // Speech & Voice State
  allToothData: [], // Array of { toothNumber, values, timestamp }
  isListening: false,
  recognizedText: '',
  voiceError: null,
  voiceLog: [], // Entries parsed in current voice session

  // Model Training Panel State
  isTrainingModel: false,
  aiTrainingStatus: 'Connected to Firebase Database',
  aiModelAccuracy: 95.0,
  isCustomModelTrained: false,

  // Report Archive
  reports: [
    { id: 'R-8801', patient: 'Sarah Johnson', date: '31-May-2026', verdict: 'Moderate Periodontitis', stage: 'Stage II, Grade B', ppd: 4.2, bop: 45 },
    { id: 'R-7740', patient: 'Michael Chen', date: '28-May-2026', verdict: 'Healthy Periodontium', stage: 'Healthy, Grade A', ppd: 2.1, bop: 8 }
  ],

  // Report Generation Config Toggles
  reportConfig: {
    ppdTrend: true,
    bopChart: true,
    aiProbability: true,
    clinicalVerdict: true,
    signatureBlock: true
  }
};

// --- Initialization ---
async function initApp() {
  const storage = window.SafeStorage || (typeof SafeStorage !== 'undefined' ? SafeStorage : null);

  // Load authentication state
  const isAuth = storage ? storage.getItem('pdd_is_authenticated') : null;
  if (isAuth === 'true') {
    AppState.isAuthenticated = true;
  }

  // Load probing measurements if exists
  const manualPpdStr = storage ? storage.getItem('pdd_manual_ppd') : null;
  if (manualPpdStr) {
    try { AppState.manualPpd = JSON.parse(manualPpdStr); } catch (e) {}
  }
  const manualCalStr = storage ? storage.getItem('pdd_manual_cal') : null;
  if (manualCalStr) {
    try { AppState.manualCal = JSON.parse(manualCalStr); } catch (e) {}
  }
  const manualBopStr = storage ? storage.getItem('pdd_manual_bop') : null;
  if (manualBopStr) {
    try { AppState.manualBop = JSON.parse(manualBopStr); } catch (e) {}
  }
  const voiceLogStr = storage ? storage.getItem('pdd_voice_log') : null;
  if (voiceLogStr) {
    try { AppState.voiceLog = JSON.parse(voiceLogStr); } catch (e) {}
  }
  const allToothDataStr = storage ? storage.getItem('pdd_all_tooth_data') : null;
  if (allToothDataStr) {
    try { AppState.allToothData = JSON.parse(allToothDataStr); } catch (e) {}
  }

  // Load local storage patients if exists
  const storedPatients = storage ? storage.getItem('pdd_patients') : null;
  if (storedPatients) {
    AppState.patients = JSON.parse(storedPatients);
  } else {
    // Populate default lists
    AppState.patients = [
      { name: "Sarah Johnson", email: "sarah@gmail.com", phone: "555-0199", history: "Moderate periodontal pocket depths, BOP active." },
      { name: "Michael Chen", email: "michael@gmail.com", phone: "555-0144", history: "Healthy gums, routine deep scaling cleanups." },
      { name: "Emily Davis", email: "emily@gmail.com", phone: "555-0182", history: "Mild gingival irritation, regular checkups." }
    ];
    if (storage) storage.setItem('pdd_patients', JSON.stringify(AppState.patients));
  }

  // Load user profile details if registered
  const registeredUser = storage ? storage.getItem('pdd_registered_user') : null;
  if (registeredUser) {
    const user = JSON.parse(registeredUser);
    AppState.clinicalUserName = user.name;
    AppState.clinicalUserEmail = user.email;
    AppState.clinicalUserPhone = user.phone;
  }

  // Try to load patients from the backend if server is online
  try {
    const backendPatients = await ApiService.getPatients();
    if (backendPatients && backendPatients.length > 0) {
      // Merge backend patients
      const patientNames = AppState.patients.map(p => p.name);
      backendPatients.forEach(bp => {
        if (!patientNames.includes(bp.name)) {
          AppState.patients.push({
            name: bp.name,
            email: bp.email || '',
            phone: bp.phone || '',
            history: bp.history || ''
          });
        }
      });
      SafeStorage.setItem('pdd_patients', JSON.stringify(AppState.patients));
    }
  } catch (e) {
    console.log('[App] Offline mode active on startup. Backend sync deferred.');
  }

  // Trigger Router
  window.addEventListener('hashchange', router);
  router();
}

// --- Dynamic Route Handler ---
function router() {
  const hash = window.location.hash || '#splash';
  const appContainer = document.getElementById('app');
  if (!appContainer) return;

  // Toggle active class in navigation sidebar
  updateActiveSidebarItem(hash);

  // Close navigation drawer by default on navigation
  toggleSidebar(false);

  // 1. Splash Screen
  if (hash === '#splash') {
    appContainer.innerHTML = renderSplash();
    setTimeout(() => {
      window.location.hash = '#onboarding';
    }, 2000);
    return;
  }

  // 2. Onboarding Screen
  if (hash === '#onboarding') {
    appContainer.innerHTML = renderOnboarding();
    return;
  }

  // 3. Login Screen
  if (hash === '#login') {
    appContainer.innerHTML = renderLogin();
    return;
  }

  // 4. Sign Up Screen
  if (hash === '#signup') {
    appContainer.innerHTML = renderSignup();
    return;
  }

  // 5. Forgot Password Screen
  if (hash === '#forgot-password') {
    appContainer.innerHTML = renderForgotPassword();
    return;
  }

  // 6. Recovery Success Screen
  if (hash === '#recovery-success') {
    appContainer.innerHTML = renderRecoverySuccess();
    return;
  }

  // Route protection
  if (!AppState.isAuthenticated && !['#splash', '#onboarding', '#login', '#signup', '#forgot-password', '#recovery-success'].includes(hash)) {
    window.location.hash = '#login';
    return;
  }

  // 7. Main Dashboard Screen
  if (hash === '#dashboard') {
    appContainer.innerHTML = renderDashboard();
    // Render curved trend chart
    ChartService.drawActivityTrend('dashboard-activity-chart');
    return;
  }

  // 8. Notifications Screen
  if (hash === '#notifications') {
    appContainer.innerHTML = renderNotifications();
    return;
  }

  // 9. Patient Registration Screen
  if (hash === '#patient-registration') {
    appContainer.innerHTML = renderPatientRegistration();
    return;
  }

  // 10. Registration Success Screen
  if (hash === '#registration-success') {
    appContainer.innerHTML = renderRegistrationSuccess();
    return;
  }

  // 11. Start Examination Screen
  if (hash === '#start-examination') {
    appContainer.innerHTML = renderStartExamination();
    return;
  }

  // 12. Examination Instructions Screen
  if (hash.startsWith('#examination-instructions/')) {
    const patientName = decodeURIComponent(hash.split('/')[1] || 'Patient');
    appContainer.innerHTML = renderExaminationInstructions(patientName);
    return;
  }

  // 13. Voice Probing Input Screen
  if (hash.startsWith('#voice-input/')) {
    const patientName = decodeURIComponent(hash.split('/')[1] || 'Patient');
    appContainer.innerHTML = renderVoiceInput(patientName);
    updateVoiceWaveform(AppState.isListening);
    return;
  }

  // 14. Manual Keypad Probing Grid Screen
  if (hash.startsWith('#manual-input/')) {
    const patientName = decodeURIComponent(hash.split('/')[1] || 'Patient');
    appContainer.innerHTML = renderManualInput(patientName);
    return;
  }

  // 15. Processing Screen
  if (hash === '#processing-examination') {
    appContainer.innerHTML = renderProcessingExamination();
    setTimeout(() => {
      window.location.hash = '#statistics-summary';
    }, 2500);
    return;
  }

  // 16. Statistics Summary Screen
  if (hash === '#statistics-summary') {
    appContainer.innerHTML = renderStatisticsSummary();
    return;
  }

  // 17. AI Classification Screen
  if (hash === '#ai-classification') {
    appContainer.innerHTML = renderAIClassification();
    return;
  }

  // 18. Probability Breakdown Screen
  if (hash === '#probability-breakdown') {
    appContainer.innerHTML = renderProbabilityBreakdown();
    ChartService.drawDonutChart('probability-donut-chart', AppState.probHealthy, AppState.probModerate, AppState.probSevere);
    return;
  }

  // 19. Generate Report Screen
  if (hash === '#generate-report') {
    appContainer.innerHTML = renderGenerateReport();
    return;
  }

  // 20. Report Preview Screen
  if (hash === '#report-preview') {
    appContainer.innerHTML = renderReportPreview();
    return;
  }

  // 21. Reports Archive Screen
  if (hash === '#reports-archive') {
    appContainer.innerHTML = renderReportsArchive();
    return;
  }

  // 22. Patient Directory Screen
  if (hash === '#patient-directory') {
    appContainer.innerHTML = renderPatientDirectory();
    return;
  }

  // 23. User Profile Screen
  if (hash === '#user-profile') {
    appContainer.innerHTML = renderUserProfile();
    return;
  }

  // 24. System Settings Screen
  if (hash === '#settings') {
    appContainer.innerHTML = renderSettings();
    return;
  }

  // 25. Filter Dashboard Screen
  if (hash === '#filter-dashboard') {
    appContainer.innerHTML = renderFilterDashboard();
    return;
  }

  // Default Fallback
  window.location.hash = '#dashboard';
}

// --- Screen Render Templates ---

function renderSplash() {
  return `
    <div class="splash-screen">
      <div class="splash-logo">🦷</div>
      <h1 class="splash-title">Pocket Depth AI</h1>
      <p class="splash-subtitle">Clinical Examination Suite</p>
      <div class="loading-dots">
        <div class="loading-dot"></div>
        <div class="loading-dot"></div>
        <div class="loading-dot"></div>
      </div>
    </div>
  `;
}

function renderOnboarding() {
  return `
    <div class="onboarding-screen">
      <div class="onboarding-logo" style="font-size: 80px; text-align: center;">🧠</div>
      <h1 class="onboarding-title">AI-Powered Periodontal Exams</h1>
      <p class="onboarding-subtitle">Fast, accurate charting using clinical speech recognition and smart models.</p>
      
      <div style="width: 100%; max-width: 440px;">
        <div class="feature-card">
          <div class="feature-icon">🎙️</div>
          <div>
            <h3 class="feature-title">Clinical Voice Recognition</h3>
            <p class="feature-desc">Speak depths directly into the chart. Hands-free efficiency.</p>
          </div>
        </div>
        <div class="feature-card">
          <div class="feature-icon">📈</div>
          <div>
            <h3 class="feature-title">Interactive Probing Grid</h3>
            <p class="feature-desc">Visual 32-tooth matrix with custom smart touch keypads.</p>
          </div>
        </div>
        <div class="feature-card">
          <div class="feature-icon">🧠</div>
          <div>
            <h3 class="feature-title">AAP/EFP 2018 Guidelines</h3>
            <p class="feature-desc">Automatic diagnostic grading, staging, and reports generation.</p>
          </div>
        </div>
        
        <button class="btn btn-white mt-lg" onclick="window.location.hash = '#login'">
          Get Started <span class="btn-icon">→</span>
        </button>
      </div>
    </div>
  `;
}

function renderLogin() {
  return `
    <div class="auth-bg">
      <div class="auth-card">
        <div class="auth-logo" style="font-size: 64px; text-align: center;">🦷</div>
        <h2 class="auth-title">Pocket Depth AI</h2>
        <p class="auth-subtitle">Sign in to your clinical database</p>
        
        <div id="login-error" class="text-error fs-sm text-center mb-md hidden" style="font-weight: 600;"></div>
        
        <form onsubmit="handleLogin(event)">
          <div class="form-group">
            <label class="form-label">CLINICAL EMAIL</label>
            <div class="input-wrapper">
              <span class="input-icon">✉️</span>
              <input type="email" id="login-email" class="input-field" placeholder="doctor@clinic.com" required value="sarah.johnson@clinical.ai">
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">PASSWORD</label>
            <div class="input-wrapper">
              <span class="input-icon">🔒</span>
              <input type="password" id="login-password" class="input-field" placeholder="••••••••" required value="password123">
            </div>
          </div>
          
          <div style="text-align: right; margin-bottom: 24px;">
            <a href="#forgot-password" class="fs-sm fw-bold">Forgot password?</a>
          </div>
          
          <button type="submit" class="btn btn-gradient">Sign In</button>
        </form>
        
        <div style="text-align: center; margin-top: 24px;" class="fs-sm">
          New clinic member? <a href="#signup" class="fw-bold">Request Registration</a>
        </div>

        <div style="text-align: center; margin-top: 18px;">
          <button type="button" class="btn btn-text fs-sm" onclick="toggleLoginFirebaseConfig()" style="color: var(--primary); font-weight: 700; display: inline-flex; align-items: center; gap: 4px; border: none; background: none; cursor: pointer; padding: 4px 8px;">
            ⚙️ Firebase Cloud Settings
          </button>
        </div>

        <div id="login-fb-config-container" class="hidden" style="margin-top: 16px; padding: 16px; border: 1px solid var(--divider); border-radius: var(--radius-md); background: rgba(0,0,0,0.02); text-align: left;">
          <h4 style="font-weight: 800; font-size: 13px; margin-bottom: 12px; display: flex; align-items: center; gap: 6px; color: var(--text-primary);">
            🔥 Firebase Project Configuration
          </h4>
          
          <div class="form-group" style="margin-bottom: 10px;">
            <label class="form-label" style="font-size: 10px; margin-bottom: 4px;">API KEY</label>
            <input type="text" id="login-fb-apiKey" class="input-field" style="padding: 8px 12px; font-size: 13px;" placeholder="AIzaSy...">
          </div>
          
          <div class="form-group" style="margin-bottom: 10px;">
            <label class="form-label" style="font-size: 10px; margin-bottom: 4px;">AUTH DOMAIN</label>
            <input type="text" id="login-fb-authDomain" class="input-field" style="padding: 8px 12px; font-size: 13px;" placeholder="project.firebaseapp.com">
          </div>
          
          <div class="form-group" style="margin-bottom: 10px;">
            <label class="form-label" style="font-size: 10px; margin-bottom: 4px;">PROJECT ID</label>
            <input type="text" id="login-fb-projectId" class="input-field" style="padding: 8px 12px; font-size: 13px;" placeholder="your-project-id">
          </div>
          
          <div class="form-group" style="margin-bottom: 12px;">
            <label class="form-label" style="font-size: 10px; margin-bottom: 4px;">APP ID</label>
            <input type="text" id="login-fb-appId" class="input-field" style="padding: 8px 12px; font-size: 13px;" placeholder="1:123:web:abc...">
          </div>
          
          <div style="display: flex; gap: 8px; align-items: center; margin-bottom: 14px;">
            <input type="checkbox" id="login-fb-enabled" style="cursor: pointer; width: 16px; height: 16px;">
            <label for="login-fb-enabled" style="font-size: 12px; font-weight: 700; cursor: pointer; color: var(--text-primary); margin: 0;">Enable Firebase Cloud Sync</label>
          </div>
          
          <button type="button" class="btn btn-primary" onclick="handleSaveLoginFirebaseConfig()" style="font-size: 12px; padding: 8px 16px; width: auto; height: auto;">Save & Apply</button>
        </div>

        <div class="divider" style="margin: 20px 0; opacity: 0.5;"></div>
        <button type="button" class="btn btn-outline" style="width: 100%;" onclick="handleOfflineBypass()">Run in Offline Mode</button>
      </div>
    </div>
  `;
}

function renderSignup() {
  return `
    <div class="auth-bg">
      <div class="auth-card">
        <h2 class="auth-title">Register Clinician</h2>
        <p class="auth-subtitle">Create your clinical staff profile</p>
        
        <div id="signup-error" class="text-error fs-sm text-center mb-md hidden" style="font-weight: 600;"></div>
        
        <form onsubmit="handleSignup(event)">
          <div class="form-group">
            <label class="form-label">FULL CLINICAL NAME</label>
            <div class="input-wrapper">
              <span class="input-icon">👤</span>
              <input type="text" id="signup-name" class="input-field" placeholder="Dr. Sarah Johnson" required>
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">CLINIC EMAIL ADDRESS</label>
            <div class="input-wrapper">
              <span class="input-icon">✉️</span>
              <input type="email" id="signup-email" class="input-field" placeholder="sarah.j@clinic.com" required>
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">PHONE NUMBER</label>
            <div class="input-wrapper">
              <span class="input-icon">📞</span>
              <input type="tel" id="signup-phone" class="input-field" placeholder="555-0199" required>
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">PASSWORD</label>
            <div class="input-wrapper">
              <span class="input-icon">🔒</span>
              <input type="password" id="signup-password" class="input-field" placeholder="Minimum 8 characters" minlength="8" required>
            </div>
          </div>
          
          <button type="submit" class="btn btn-gradient mt-md">Request Account</button>
        </form>
        
        <div style="text-align: center; margin-top: 24px;" class="fs-sm">
          Already registered? <a href="#login" class="fw-bold">Sign In</a>
        </div>

        <div style="text-align: center; margin-top: 18px;">
          <button type="button" class="btn btn-text fs-sm" onclick="toggleLoginFirebaseConfig('signup')" style="color: var(--primary); font-weight: 700; display: inline-flex; align-items: center; gap: 4px; border: none; background: none; cursor: pointer; padding: 4px 8px;">
            ⚙️ Firebase Cloud Settings
          </button>
        </div>

        <div id="signup-fb-config-container" class="hidden" style="margin-top: 16px; padding: 16px; border: 1px solid var(--divider); border-radius: var(--radius-md); background: rgba(0,0,0,0.02); text-align: left;">
          <h4 style="font-weight: 800; font-size: 13px; margin-bottom: 12px; display: flex; align-items: center; gap: 6px; color: var(--text-primary);">
            🔥 Firebase Project Configuration
          </h4>
          
          <div class="form-group" style="margin-bottom: 10px;">
            <label class="form-label" style="font-size: 10px; margin-bottom: 4px;">API KEY</label>
            <input type="text" id="signup-fb-apiKey" class="input-field" style="padding: 8px 12px; font-size: 13px;" placeholder="AIzaSy...">
          </div>
          
          <div class="form-group" style="margin-bottom: 10px;">
            <label class="form-label" style="font-size: 10px; margin-bottom: 4px;">AUTH DOMAIN</label>
            <input type="text" id="signup-fb-authDomain" class="input-field" style="padding: 8px 12px; font-size: 13px;" placeholder="project.firebaseapp.com">
          </div>
          
          <div class="form-group" style="margin-bottom: 10px;">
            <label class="form-label" style="font-size: 10px; margin-bottom: 4px;">PROJECT ID</label>
            <input type="text" id="signup-fb-projectId" class="input-field" style="padding: 8px 12px; font-size: 13px;" placeholder="your-project-id">
          </div>
          
          <div class="form-group" style="margin-bottom: 12px;">
            <label class="form-label" style="font-size: 10px; margin-bottom: 4px;">APP ID</label>
            <input type="text" id="signup-fb-appId" class="input-field" style="padding: 8px 12px; font-size: 13px;" placeholder="1:123:web:abc...">
          </div>
          
          <div style="display: flex; gap: 8px; align-items: center; margin-bottom: 14px;">
            <input type="checkbox" id="signup-fb-enabled" style="cursor: pointer; width: 16px; height: 16px;">
            <label for="signup-fb-enabled" style="font-size: 12px; font-weight: 700; cursor: pointer; color: var(--text-primary); margin: 0;">Enable Firebase Cloud Sync</label>
          </div>
          
          <button type="button" class="btn btn-primary" onclick="handleSaveLoginFirebaseConfig('signup')" style="font-size: 12px; padding: 8px 16px; width: auto; height: auto;">Save & Apply</button>
        </div>

        <div class="divider" style="margin: 20px 0; opacity: 0.5;"></div>
        <button type="button" class="btn btn-outline" style="width: 100%;" onclick="handleOfflineBypass()">Run in Offline Mode</button>
      </div>
    </div>
  `;
}

function renderForgotPassword() {
  return `
    <div class="auth-bg">
      <div class="auth-card">
        <h2 class="auth-title">Reset Password</h2>
        <p class="auth-subtitle">Verify your registered clinical email</p>
        
        <form onsubmit="handleForgotPassword(event)">
          <div class="form-group">
            <label class="form-label">CLINICAL EMAIL</label>
            <div class="input-wrapper">
              <span class="input-icon">✉️</span>
              <input type="email" id="forgot-email" class="input-field" placeholder="doctor@clinic.com" required>
            </div>
          </div>
          
          <button type="submit" class="btn btn-gradient mt-md">Send Recovery Link</button>
        </form>
        
        <div style="text-align: center; margin-top: 24px;" class="fs-sm">
          Remember credentials? <a href="#login" class="fw-bold">Sign In</a>
        </div>
      </div>
    </div>
  `;
}

function renderRecoverySuccess() {
  return `
    <div class="success-screen">
      <div class="success-icon">✔️</div>
      <h2 class="success-title">Recovery Sent</h2>
      <p class="success-desc">A reset link has been dispatched to your email address. Verify and log in again.</p>
      <button class="btn btn-primary" onclick="window.location.hash = '#login'">Return to Sign In</button>
    </div>
  `;
}

function renderDashboard() {
  return `
    <div class="app-shell">
      ${renderSidebarMarkup()}
      <div class="main-content">
        ${renderTopBarMarkup('Pocket Depth AI')}
        
        <div class="page">
          <div class="page-header flex justify-between items-center">
            <div>
              <p class="page-subtitle">Welcome back,</p>
              <h2 class="page-title">Dr. ${AppState.clinicalUserName}</h2>
            </div>
            <div style="text-align: right;">
              <span class="stat-badge" style="background: ${ApiService.isFirebaseConnected() ? 'var(--success-bg)' : 'rgba(255,152,0,0.15)'}; color: ${ApiService.isFirebaseConnected() ? 'var(--success)' : 'rgba(255,152,0,1)'}; font-weight: 700; font-size: 11px;">
                ${ApiService.isFirebaseConnected() ? '🟢 CLOUD CONNECTED' : '🟡 OFFLINE MODE'}
              </span>
            </div>
          </div>
          
          <div class="page-body">
            <!-- Stat Widgets Row -->
            <div style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; margin-bottom: 20px;">
              <div class="card" style="padding: 16px;">
                <div class="stat-icon" style="background: rgba(93,72,209,0.1); color: var(--primary); width: 40px; height: 40px; font-size: 18px; margin-bottom: 8px;">🦷</div>
                <div class="stat-value" style="font-size: 20px;">32</div>
                <div class="stat-label">Teeth Standard Chart</div>
              </div>
              <div class="card" style="padding: 16px;">
                <div class="stat-icon" style="background: rgba(76,175,80,0.1); color: var(--success); width: 40px; height: 40px; font-size: 18px; margin-bottom: 8px;">👥</div>
                <div class="stat-value" style="font-size: 20px;">${AppState.patients.length}</div>
                <div class="stat-label">Patients Monitored</div>
              </div>
            </div>

            <!-- Activity Chart Card -->
            <div class="card mb-lg">
              <div class="card-body">
                <h3 class="section-label" style="margin-bottom: 8px;">Clinical Probing Activity</h3>
                <div class="chart-container">
                  <canvas id="dashboard-activity-chart"></canvas>
                </div>
              </div>
            </div>

            <!-- Quick Actions -->
            <h3 class="section-label">Quick Actions</h3>
            <div class="quick-action quick-action-gradient" onclick="window.location.hash = '#start-examination'">
              <div class="quick-action-icon">🎙️</div>
              <div class="quick-action-info">
                <h4 class="quick-action-title">Start Examination</h4>
                <p class="quick-action-sub">Initialize Speech recognition probe charting</p>
              </div>
              <span class="quick-action-arrow">→</span>
            </div>
            
            <div class="quick-action btn-outline" style="border: 1px solid var(--divider); background: white; margin-bottom: 20px;" onclick="window.location.hash = '#patient-registration'">
              <div class="quick-action-icon" style="background: var(--primary-container); color: var(--primary);">👤</div>
              <div class="quick-action-info">
                <h4 class="quick-action-title" style="color: var(--text-primary);">Register New Patient</h4>
                <p class="quick-action-sub" style="color: var(--text-secondary);">Archive credentials and systemic history</p>
              </div>
              <span class="quick-action-arrow" style="color: var(--primary);">→</span>
            </div>


          </div>
        </div>
      </div>
    </div>
  `;
}

function renderNotifications() {
  return `
    <div class="app-shell">
      ${renderSidebarMarkup()}
      <div class="main-content">
        ${renderTopBarMarkup('Notifications')}
        <div class="page">
          <div class="page-header">
            <h2 class="page-title">Notifications</h2>
            <p class="page-subtitle">Historical model and storage log sync updates</p>
          </div>
          <div class="page-body">
            ${AppState.notifications.map(n => `
              <div class="notif-item">
                <div class="notif-icon" style="background: ${n.bg}; color: ${n.color};">${n.icon}</div>
                <div class="notif-body">
                  <h4 class="notif-title">${n.title}</h4>
                  <p class="notif-desc">${n.desc}</p>
                </div>
                <div class="notif-time">${n.time}</div>
              </div>
            `).join('')}
          </div>
        </div>
      </div>
    </div>
  `;
}

function renderPatientRegistration() {
  return `
    <div class="app-shell">
      ${renderSidebarMarkup()}
      <div class="main-content">
        ${renderTopBarMarkup('Register Patient')}
        <div class="page">
          <div class="page-header">
            <h2 class="page-title">Patient Profile</h2>
            <p class="page-subtitle">Save standard dental history and clinical information</p>
          </div>
          
          <div class="page-body">
            <form onsubmit="handlePatientRegistration(event)">
              <div class="form-group">
                <label class="form-label">PATIENT FULL NAME</label>
                <div class="input-wrapper">
                  <span class="input-icon">👤</span>
                  <input type="text" id="patient-reg-name" class="input-field" placeholder="Full name" required>
                </div>
              </div>
              <div class="form-group">
                <label class="form-label">EMAIL ADDRESS</label>
                <div class="input-wrapper">
                  <span class="input-icon">✉️</span>
                  <input type="email" id="patient-reg-email" class="input-field" placeholder="name@domain.com" required>
                </div>
              </div>
              <div class="form-group">
                <label class="form-label">CONTACT PHONE NUMBER</label>
                <div class="input-wrapper">
                  <span class="input-icon">📞</span>
                  <input type="tel" id="patient-reg-phone" class="input-field" placeholder="Phone digits" required>
                </div>
              </div>
              <div class="form-group">
                <label class="form-label">SYSTEMIC & DENTAL HISTORY</label>
                <textarea id="patient-reg-history" class="input-field input-no-icon" placeholder="Describe any past operations, allergies, or chronic illness notes..."></textarea>
              </div>
              
              <div class="page-actions" style="padding: 24px 0 0;">
                <button type="submit" class="btn btn-primary" style="flex: 1;">Register Patient</button>
                <button type="button" class="btn btn-outline" style="flex: 1;" onclick="window.location.hash = '#dashboard'">Cancel</button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  `;
}

function renderRegistrationSuccess() {
  return `
    <div class="success-screen">
      <div class="success-icon" style="background: var(--success-bg); color: var(--success);">✔️</div>
      <p class="success-desc">${ApiService.isFirebaseConnected() ? 'Standard profile synced successfully to Firebase Cloud Firestore.' : 'Standard profile registered locally in offline mode.'}</p>
      
      <div style="width: 100%; max-width: 320px; display: flex; flex-direction: column; gap: 12px;">
        <button class="btn btn-primary" onclick="window.location.hash = '#start-examination'">Start Examination</button>
        <button class="btn btn-outline" onclick="window.location.hash = '#patient-registration'">Register Another</button>
        <button class="btn btn-text" onclick="window.location.hash = '#patient-directory'">View Patient Directory</button>
      </div>
    </div>
  `;
}

function renderStartExamination() {
  return `
    <div class="app-shell">
      ${renderSidebarMarkup()}
      <div class="main-content">
        ${renderTopBarMarkup('Start Exam')}
        <div class="page">
          <div class="page-header">
            <h2 class="page-title">Active Directory</h2>
            <p class="page-subtitle">Select a registered patient to begin probing examination</p>
          </div>
          
          <div class="page-body">
            <div class="form-group">
              <div class="input-wrapper">
                <span class="input-icon">🔍</span>
                <input type="text" id="patient-search-input" class="input-field" placeholder="Search patients..." oninput="filterStartExaminationList()">
              </div>
            </div>
            
            <div id="start-examination-list" style="margin-top: 20px;">
              ${AppState.patients.map(p => `
                <div class="list-item" onclick="handleStartPatientExam('${encodeURIComponent(p.name)}')">
                  <div class="avatar avatar-purple">${p.name.charAt(0)}</div>
                  <div class="list-info">
                    <h4 class="list-title">${p.name}</h4>
                    <p class="list-subtitle">${p.phone || 'No phone'}</p>
                  </div>
                  <span class="list-arrow">→</span>
                </div>
              `).join('')}
            </div>

            <button class="btn btn-outline mt-lg" onclick="window.location.hash = '#patient-registration'">
              ➕ Register New Patient
            </button>
          </div>
        </div>
      </div>
    </div>
  `;
}

function renderExaminationInstructions(patientName) {
  return `
    <div class="app-shell">
      ${renderSidebarMarkup()}
      <div class="main-content">
        ${renderTopBarMarkup('Instructions')}
        <div class="page">
          <div class="page-header">
            <p class="page-subtitle">Preparing exam for ${patientName}</p>
            <h2 class="page-title">Speech Guidelines</h2>
          </div>
          
          <div class="page-body">
            <p class="text-gray mb-lg">Follow these rules to feed periodontal pocket depths directly into the charting grid using your microphone:</p>
            
            <div class="instruction-card">
              <div class="instruction-num">1</div>
              <div>
                <h4 class="instruction-title">State the Tooth Number</h4>
                <p class="instruction-desc">Always start a sequence by identifying the target tooth (1 to 32). E.g., <strong>"Tooth 11"</strong> or <strong>"Tooth 2"</strong>.</p>
              </div>
            </div>
            
            <div class="instruction-card">
              <div class="instruction-num">2</div>
              <div>
                <h4 class="instruction-title">Say the 3 Probing Values</h4>
                <p class="instruction-desc">Speak the three measurement depths sequentially. E.g., <strong>"Three, Four, Five"</strong> (representing Distobuccal, Buccal, Mesiobuccal sites).</p>
              </div>
            </div>

            <div class="instruction-card">
              <div class="instruction-num">3</div>
              <div>
                <h4 class="instruction-title">Continuous Feedback Loop</h4>
                <p class="instruction-desc">Listen for confirmation chimes and visually inspect the values logged in real time.</p>
              </div>
            </div>

            <!-- Pocket Depth Guide -->
            <h3 class="section-label mt-lg">Clinical Severity Guide</h3>
            <div class="range-chips mb-lg">
              <div class="range-chip" style="background: var(--success-bg); color: var(--success);">
                1-3 mm
                <div class="range-chip-label">Healthy</div>
              </div>
              <div class="range-chip" style="background: var(--warning-bg); color: var(--warning);">
                4-5 mm
                <div class="range-chip-label">Moderate</div>
              </div>
              <div class="range-chip" style="background: var(--error-bg); color: var(--error);">
                >=6 mm
                <div class="range-chip-label">Severe</div>
              </div>
            </div>

            <div class="page-actions" style="padding: 24px 0 0;">
              <button class="btn btn-primary" style="flex: 1;" onclick="window.location.hash = '#voice-input/${encodeURIComponent(patientName)}'">
                Start Voice Exam
              </button>
              <button class="btn btn-outline" style="flex: 1;" onclick="window.location.hash = '#manual-input/${encodeURIComponent(patientName)}'">
                Skip to Manual Grid
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  `;
}

function renderVoiceInput(patientName) {
  return `
    <div class="voice-screen">
      <div class="top-bar">
        <button class="icon-btn text-white" onclick="window.location.hash = '#examination-instructions/${encodeURIComponent(patientName)}'">
          ←
        </button>
        <div class="top-bar-title text-white">Voice Exam</div>
        <div style="width: 40px;"></div>
      </div>

      <div style="text-align: center; margin-top: 40px; padding: 0 24px;">
        <h2 style="font-weight: 800; font-size: 28px;">Examining: ${patientName}</h2>
        <p style="opacity: 0.8; font-size: 14px; margin-top: 4px;">Speak tooth number followed by 3 depths</p>
      </div>

      <!-- Pulse Mic -->
      <div class="mic-container" style="margin-top: 60px;">
        <div id="mic-pulse-1" class="mic-ring mic-ring-1"></div>
        <div id="mic-pulse-2" class="mic-ring mic-ring-2"></div>
        <button id="btn-mic-toggle" class="mic-btn mic-btn-idle" onclick="toggleVoiceListening('${encodeURIComponent(patientName)}')">
          🎙️
        </button>
      </div>

      <!-- Waveform Animation -->
      <div class="waveform-bars">
        <div class="wave-bar" style="--wave-h: 24px;"></div>
        <div class="wave-bar" style="--wave-h: 12px;"></div>
        <div class="wave-bar" style="--wave-h: 30px;"></div>
        <div class="wave-bar" style="--wave-h: 18px;"></div>
        <div class="wave-bar" style="--wave-h: 26px;"></div>
        <div class="wave-bar" style="--wave-h: 10px;"></div>
        <div class="wave-bar" style="--wave-h: 28px;"></div>
      </div>

      <div style="text-align: center; margin-top: 8px;">
        <span id="voice-listening-status" style="font-weight: 700; font-size: 13px; text-transform: uppercase; letter-spacing: 1px; opacity: 0.7;">
          Mic Standby
        </span>
      </div>

      <!-- Parse Result Card -->
      <div id="voice-parse-card" class="voice-result-card voice-result-idle" style="margin-top: 32px;">
        <span id="voice-recognized-text" style="font-weight: 600; font-size: 14px; opacity: 0.9;">
          Waiting for speech input...
        </span>
      </div>

      <!-- Example guidelines -->
      <div class="example-card" style="margin-top: 24px;" onclick="simulateVoiceInput('${encodeURIComponent(patientName)}')">
        <div class="example-header">
          <div class="example-title">💡 Need example?</div>
          <span style="font-size: 12px; opacity: 0.7;">Tap to Simulate Speech</span>
        </div>
        <div class="example-body">
          <div class="voice-example-row">
            <span class="voice-example-cmd">"Tooth 11: 3, 4, 5"</span>
            <span class="voice-example-result">Logs Tooth 11 [3mm, 4mm, 5mm]</span>
          </div>
          <div class="voice-example-row">
            <span class="voice-example-cmd">"Tooth 3: 2, 2, 3"</span>
            <span class="voice-example-result">Logs Tooth 3 [2mm, 2mm, 3mm]</span>
          </div>
        </div>
      </div>

      <!-- Log entries list -->
      <div class="voice-entry-card" style="margin-top: 24px; max-height: 180px; overflow-y: auto;">
        <h4 id="voice-log-header" style="font-size: 13px; font-weight: 700; text-transform: uppercase; opacity: 0.8; margin-bottom: 10px;">
          Session Measurements Log (${AppState.voiceLog.length})
        </h4>
        <div id="voice-session-log-list">
          ${AppState.voiceLog.length === 0 
            ? '<p style="font-size: 13px; opacity: 0.6; text-align: center; padding: 12px 0;">No sites logged yet in this session</p>'
            : AppState.voiceLog.map(v => `
              <div class="voice-entry-inner">
                <div class="tooth-badge">${v.toothNumber}</div>
                <div style="font-weight: 700; font-size: 14px;">Values: ${v.values.join(' - ')} mm</div>
                <div class="depth-badge" style="background: ${Math.max(...v.values) >= 6 ? 'var(--error-bg)' : Math.max(...v.values) >= 4 ? 'var(--warning-bg)' : 'var(--success-bg)'}; color: ${Math.max(...v.values) >= 6 ? 'var(--error)' : Math.max(...v.values) >= 4 ? 'var(--warning)' : 'var(--success)'};">
                  ${Math.max(...v.values) >= 6 ? 'Severe' : Math.max(...v.values) >= 4 ? 'Moderate' : 'Healthy'}
                </div>
              </div>
            `).join('')
          }
        </div>
      </div>

      <div style="padding: 24px; display: flex; gap: 16px;">
        <button class="btn btn-white btn-lg" style="flex: 1;" onclick="handleVoiceFinish()">
          Calculate AI Staging
        </button>
      </div>
    </div>
  `;
}

function renderManualInput(patientName) {
  // Build tooth components 1 to 32
  let toothCardsHtml = '';
  for (let t = 1; t <= 32; t++) {
    const ppd0 = AppState.manualPpd[`tooth${t}_ppd_0`] || '';
    const ppd1 = AppState.manualPpd[`tooth${t}_ppd_1`] || '';
    const ppd2 = AppState.manualPpd[`tooth${t}_ppd_2`] || '';

    toothCardsHtml += `
      <div class="tooth-card">
        <div class="tooth-header">
          <h4 class="tooth-title" style="color: var(--text-primary);">Teeth ${t}</h4>
          <span class="tooth-edit-icon">✏️</span>
        </div>
        
        <!-- PPD Cells -->
        <p class="tooth-ppd-label">Pocket Depth (PPD)</p>
        <div class="ppd-cells">
          <div id="cell-tooth${t}_ppd_0" class="ppd-cell ${ppd0 ? 'filled' : ''}" onclick="focusManualCell('tooth${t}_ppd_0')">
            ${ppd0 || '-'}
          </div>
          <div id="cell-tooth${t}_ppd_1" class="ppd-cell ${ppd1 ? 'filled' : ''}" onclick="focusManualCell('tooth${t}_ppd_1')">
            ${ppd1 || '-'}
          </div>
          <div id="cell-tooth${t}_ppd_2" class="ppd-cell ${ppd2 ? 'filled' : ''}" onclick="focusManualCell('tooth${t}_ppd_2')">
            ${ppd2 || '-'}
          </div>
        </div>
      </div>
    `;
  }

  return `
    <div class="voice-screen" style="min-height: 100vh; overflow-y: auto; padding-bottom: 120px;">
      <!-- Custom Header -->
      <div class="top-bar" style="padding: 16px 24px; display: flex; align-items: center; justify-content: space-between; background: rgba(0,0,0,0.15);">
        <button class="icon-btn text-white" style="background: none; border: none; font-size: 20px; cursor: pointer; color: white;" onclick="window.location.hash = '#dashboard'">
          ←
        </button>
        <div class="top-bar-title text-white" style="font-weight: 700; font-size: 16px; letter-spacing: 1px;">PERIODONTAL CHARTING</div>
        <button class="btn btn-white btn-sm" onclick="clearAllProbingData()" style="color: var(--primary); font-weight: 700; border: none; padding: 6px 12px; border-radius: 8px; font-size: 11px; cursor: pointer; background: white;">
          🗑️ Clear
        </button>
      </div>

      <!-- Top Info Section -->
      <div style="text-align: center; margin-top: 24px; padding: 0 24px;">
        <h2 style="font-weight: 800; font-size: 24px; color: white; margin-bottom: 4px;">Examining: ${patientName}</h2>
        <p style="opacity: 0.8; font-size: 13px; color: white;">Speak tooth depths (e.g. "Tooth 11: 3, 4, 5") or tap cells to enter manually</p>
      </div>

      <!-- Unified Voice Panel (Mic + Waveform + Recognized Card) -->
      <div class="flex flex-col items-center" style="margin-top: 30px; display: flex; flex-direction: column; align-items: center;">
        <!-- Pulse Mic -->
        <div class="mic-container" style="position: relative; width: 140px; height: 140px; margin: 0 auto; display: flex; align-items: center; justify-content: center;">
          <div id="manual-mic-pulse-1" class="mic-ring mic-ring-1" style="width: 140px; height: 140px; border-color: rgba(255,255,255,0.3); border-radius: 50%; position: absolute;"></div>
          <div id="manual-mic-pulse-2" class="mic-ring mic-ring-2" style="width: 120px; height: 120px; border-color: rgba(255,255,255,0.15); border-radius: 50%; position: absolute;"></div>
          <button id="btn-manual-mic-toggle" class="mic-btn mic-btn-idle" style="width: 80px; height: 80px; font-size: 32px; border-radius: 50%; border: none; cursor: pointer; display: flex; align-items: center; justify-content: center; position: relative; z-index: 1;" onclick="toggleManualVoiceListening('${encodeURIComponent(patientName)}')">
            🎙️
          </button>
        </div>

        <!-- Waveform Animation -->
        <div class="waveform-bars" style="margin-top: 16px; display: flex; gap: 4px; justify-content: center; align-items: center; height: 36px;">
          <div class="wave-bar" style="--wave-h: 24px;"></div>
          <div class="wave-bar" style="--wave-h: 12px;"></div>
          <div class="wave-bar" style="--wave-h: 30px;"></div>
          <div class="wave-bar" style="--wave-h: 18px;"></div>
          <div class="wave-bar" style="--wave-h: 26px;"></div>
          <div class="wave-bar" style="--wave-h: 10px;"></div>
          <div class="wave-bar" style="--wave-h: 28px;"></div>
        </div>

        <div style="text-align: center; margin-top: 8px;">
          <span id="manual-voice-listening-status" style="font-weight: 700; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; opacity: 0.8; color: white;">
            Mic Standby
          </span>
        </div>

        <!-- Parse Result Card -->
        <div id="manual-voice-parse-card" class="voice-result-card voice-result-idle" style="margin-top: 16px; width: calc(100% - 48px); max-width: 440px;">
          <span id="manual-voice-recognized-text" style="font-weight: 600; font-size: 13px; opacity: 0.9; color: white;">
            Waiting for speech input...
          </span>
        </div>

        <!-- Simulation Button -->
        <div style="margin-top: 12px;">
          <button class="btn btn-white btn-sm" onclick="simulateManualVoiceInput('${encodeURIComponent(patientName)}')" style="color: var(--primary); font-weight: 700; padding: 6px 16px; font-size: 12px; border-radius: 20px; border: none; cursor: pointer; background: white;">
            🎲 Simulate Speech Input
          </button>
        </div>
      </div>

      <!-- The 32 Teeth Grid -->
      <div style="padding: 24px 16px 0; max-width: 1100px; margin: 0 auto;">
        <div class="tooth-grid">
          ${toothCardsHtml}
        </div>
      </div>

      <!-- Bottom Compute Button -->
      <div style="padding: 32px 24px 0; max-width: 480px; margin: 0 auto;">
        <button class="btn btn-white btn-lg" style="width: 100%; font-weight: 800; font-size: 16px; background: white; color: var(--primary); border: none; height: 60px; border-radius: 30px; cursor: pointer;" onclick="window.location.hash = '#processing-examination'">
          Calculate AI Staging
        </button>
      </div>

      <!-- Floating Clinical Keypad -->
      <div id="manual-clinical-keypad" class="keypad" style="background: #1E1A4A; color: white;">
        <div class="keypad-header" style="border-bottom: 1px solid rgba(255,255,255,0.1); padding-bottom: 8px; display: flex; justify-content: space-between; align-items: center;">
          <span id="keypad-focused-cell-label" class="keypad-label" style="color: rgba(255,255,255,0.6); font-size: 12px; font-weight: 700;">Select a cell to enter value</span>
          <button class="btn-text" style="color: white; font-weight: bold; background: none; border: none; cursor: pointer; font-size: 14px;" onclick="closeKeypad()">Done</button>
        </div>
        <div class="keypad-keys" style="margin-top: 12px; display: flex; gap: 8px;">
          <button class="keypad-key" style="background: rgba(255,255,255,0.1); color: white;" onclick="pressKeypadValue(1)">1</button>
          <button class="keypad-key" style="background: rgba(255,255,255,0.1); color: white;" onclick="pressKeypadValue(2)">2</button>
          <button class="keypad-key" style="background: rgba(255,255,255,0.1); color: white;" onclick="pressKeypadValue(3)">3</button>
          <button class="keypad-key" style="background: rgba(255,255,255,0.1); color: white;" onclick="pressKeypadValue(4)">4</button>
          <button class="keypad-key" style="background: rgba(255,255,255,0.1); color: white;" onclick="pressKeypadValue(5)">5</button>
          <button class="keypad-key" style="background: rgba(255,255,255,0.1); color: white;" onclick="pressKeypadValue(6)">6</button>
          <button class="keypad-key" style="background: rgba(255,255,255,0.1); color: white;" onclick="pressKeypadValue(7)">7</button>
          <button class="keypad-key" style="background: rgba(255,255,255,0.1); color: white;" onclick="pressKeypadValue(8)">8</button>
          <button class="keypad-key" style="background: rgba(255,255,255,0.1); color: white;" onclick="pressKeypadValue(9)">9</button>
          <button class="keypad-key" style="background: rgba(255,255,255,0.1); color: white;" onclick="pressKeypadValue(10)">10</button>
          <button class="keypad-key" style="background: rgba(244,67,54,0.2); color: #FF5252; border: none;" onclick="pressKeypadValue('del')">⌫</button>
        </div>
      </div>
    </div>
  `;
}

function renderProcessingExamination() {
  return `
    <div class="processing-screen">
      <div class="spinner-container">
        <div class="spinner"></div>
        <div class="spinner-logo">🦷</div>
      </div>
      <p style="color: var(--text-secondary); font-size: 14px; margin-top: 8px;">${ApiService.isFirebaseConnected() ? 'Syncing clinical values with Firebase Cloud Firestore...' : 'Analyzing periodontal measurements locally...'}</p>
      
      <div class="linear-progress">
        <div class="linear-progress-bar"></div>
      </div>
    </div>
  `;
}

function renderStatisticsSummary() {
  // Determine severity coloring
  let ppdColor = 'var(--success)';
  if (AppState.meanPpd >= 5.0) ppdColor = 'var(--error)';
  else if (AppState.meanPpd >= 3.8) ppdColor = 'var(--warning)';

  let calColor = 'var(--success)';
  if (AppState.meanCal >= 4.0) calColor = 'var(--error)';
  else if (AppState.meanCal >= 2.8) calColor = 'var(--warning)';

  let bopColor = 'var(--success)';
  if (AppState.bopPercentage >= 60.0) bopColor = 'var(--error)';
  else if (AppState.bopPercentage >= 30.0) bopColor = 'var(--warning)';

  return `
    <div class="app-shell">
      ${renderSidebarMarkup()}
      <div class="main-content">
        ${renderTopBarMarkup('Statistics')}
        <div class="page">
          <div class="page-header">
            <p class="page-subtitle">Periodontal Assessment Summary</p>
            <h2 class="page-title">Clinical Metrics</h2>
          </div>
          
          <div class="page-body">
            <!-- Stat Cards -->
            <div class="stat-card">
              <div class="stat-icon" style="background: var(--primary-container); color: var(--primary);">📊</div>
              <div class="stat-info">
                <div class="stat-value" style="color: ${ppdColor}">${AppState.meanPpd} mm</div>
                <div class="stat-label">Mean Pocket Depth (PPD)</div>
              </div>
            </div>
            
            <div class="stat-card">
              <div class="stat-icon" style="background: var(--primary-container); color: var(--primary);">📈</div>
              <div class="stat-info">
                <div class="stat-value" style="color: ${calColor}">${AppState.meanCal} mm</div>
                <div class="stat-label">Mean Attachment Loss (CAL)</div>
              </div>
            </div>
            
            <div class="stat-card">
              <div class="stat-icon" style="background: var(--primary-container); color: var(--primary);">🩸</div>
              <div class="stat-info">
                <div class="stat-value" style="color: ${bopColor}">${AppState.bopPercentage}%</div>
                <div class="stat-label">Bleeding on Probing (BOP)</div>
              </div>
            </div>

            <!-- Deep pocket sites progress -->
            <div class="card mb-lg" style="margin-top: 24px;">
              <div class="card-body">
                <div class="flex justify-between items-center" style="margin-bottom: 12px;">
                  <span style="font-weight: 700; font-size: 15px;">Deep Probing Sites (>=4mm)</span>
                  <span style="font-weight: 900; font-size: 16px; color: var(--error);">${AppState.deepPocketsCount} / 96</span>
                </div>
                <div class="progress-track progress-lg">
                  <div class="progress-fill" style="width: ${(AppState.deepPocketsCount / 96) * 100}%; background: var(--error);"></div>
                </div>
                <p style="font-size: 12px; color: var(--text-secondary); margin-top: 10px; line-height: 1.4;">
                  Severe risk is flagged when pocket depth exceeds 4mm in over 15 distinct measured mouth sites.
                </p>
              </div>
            </div>

            <div class="page-actions" style="padding: 24px 0 0;">
              <button class="btn btn-primary" onclick="window.location.hash = '#ai-classification'">
                View AI Staging
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  `;
}

function renderAIClassification() {
  let color = 'var(--success)';
  let bg = 'var(--success-bg)';
  if (AppState.clinicalVerdict.includes('Severe')) {
    color = 'var(--error)';
    bg = 'var(--error-bg)';
  } else if (AppState.clinicalVerdict.includes('Moderate')) {
    color = 'var(--warning)';
    bg = 'var(--warning-bg)';
  } else if (AppState.clinicalVerdict.includes('Mild')) {
    color = 'var(--primary-light)';
    bg = 'var(--primary-container)';
  }

  return `
    <div class="app-shell">
      ${renderSidebarMarkup()}
      <div class="main-content">
        ${renderTopBarMarkup('Verdict')}
        <div class="page">
          <div class="page-header">
            <p class="page-subtitle">${ApiService.isFirebaseConnected() ? 'Firebase Cloud AI Diagnosis' : 'Local Periodontal AI Engine'}</p>
            <h2 class="page-title">Disease Stage Classification</h2>
          </div>
          
          <div class="page-body">
            
            <div class="card text-center mb-lg" style="padding: 32px 20px; background: ${bg}; border: 1px solid ${color}40;">
              <div style="font-size: 64px; margin-bottom: 16px;">🧠</div>
              <h3 style="font-size: 26px; font-weight: 900; color: ${color};">${AppState.clinicalVerdict}</h3>
              <p style="font-weight: 700; font-size: 15px; color: var(--text-secondary); margin-top: 8px;">
                AAP/EFP Guideline Stage: <span style="color: var(--text-primary);">${AppState.stageAndGrade}</span>
              </p>
            </div>

            <h3 class="section-label">Clinical Interventions</h3>
            <div class="card mb-lg">
              <div class="card-body">
                <div class="rec-item">
                  <span class="rec-icon">✔️</span>
                  <p class="rec-text">Schedule localized deep quadrant subgingival scaling root planing (SRP).</p>
                </div>
                <div class="rec-item">
                  <span class="rec-icon">✔️</span>
                  <p class="rec-text">Administer subgingival chlorhexidine antimicrobial irrigation flush.</p>
                </div>
                <div class="rec-item">
                  <span class="rec-icon">✔️</span>
                  <p class="rec-text">Re-evaluating pocket depths in 4-6 weeks for potential laser therapy.</p>
                </div>
              </div>
            </div>

            <div class="page-actions" style="padding: 24px 0 0;">
              <button class="btn btn-primary" onclick="window.location.hash = '#probability-breakdown'">
                Analyze Probabilities
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  `;
}

function renderProbabilityBreakdown() {
  return `
    <div class="app-shell">
      ${renderSidebarMarkup()}
      <div class="main-content">
        ${renderTopBarMarkup('Breakdown')}
        <div class="page">
          <div class="page-header">
            <p class="page-subtitle">Diagnostic confidence levels</p>
            <h2 class="page-title">Probability Breakdown</h2>
          </div>
          
          <div class="page-body">
            <!-- Donut Chart -->
            <div class="donut-container">
              <canvas id="probability-donut-chart"></canvas>
              <div class="donut-center">
                <span class="donut-value">${Math.max(AppState.probHealthy, AppState.probModerate, AppState.probSevere)}%</span>
                <span class="donut-label">Confidence</span>
              </div>
            </div>

            <!-- Legends -->
            <div class="legend" style="margin-bottom: 32px;">
              <div class="legend-item">
                <span class="legend-dot" style="background: var(--success);"></span>
                <span class="legend-text">Healthy</span>
              </div>
              <div class="legend-item">
                <span class="legend-dot" style="background: var(--warning);"></span>
                <span class="legend-text">Moderate</span>
              </div>
              <div class="legend-item">
                <span class="legend-dot" style="background: var(--error);"></span>
                <span class="legend-text">Severe</span>
              </div>
            </div>

            <!-- Probability Bars -->
            <div class="prob-bar">
              <div class="prob-header">
                <span class="prob-label">Healthy Baseline</span>
                <span class="prob-value">${AppState.probHealthy}%</span>
              </div>
              <div class="progress-track">
                <div class="progress-fill" style="width: ${AppState.probHealthy}%; background: var(--success);"></div>
              </div>
            </div>

            <div class="prob-bar">
              <div class="prob-header">
                <span class="prob-label">Moderate Periodontitis</span>
                <span class="prob-value">${AppState.probModerate}%</span>
              </div>
              <div class="progress-track">
                <div class="progress-fill" style="width: ${AppState.probModerate}%; background: var(--warning);"></div>
              </div>
            </div>

            <div class="prob-bar">
              <div class="prob-header">
                <span class="prob-label">Severe Periodontitis</span>
                <span class="prob-value">${AppState.probSevere}%</span>
              </div>
              <div class="progress-track">
                <div class="progress-fill" style="width: ${AppState.probSevere}%; background: var(--error);"></div>
              </div>
            </div>

            <div class="page-actions" style="padding: 24px 0 0; flex-direction: column; gap: 12px;">
              <button class="btn btn-primary" onclick="window.location.hash = '#generate-report'">
                Generate Clinical Report
              </button>
              <button class="btn btn-outline" onclick="resetExaminationState()">
                Start New Examination
              </button>
              <button class="btn btn-text" onclick="window.location.hash = '#dashboard'">
                Return to Dashboard
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  `;
}

function renderGenerateReport() {
  return `
    <div class="app-shell">
      ${renderSidebarMarkup()}
      <div class="main-content">
        ${renderTopBarMarkup('Configure Report')}
        <div class="page">
          <div class="page-header">
            <p class="page-subtitle">Configure medical report sections</p>
            <h2 class="page-title">Generate Report</h2>
          </div>
          
          <div class="page-body">
            <h3 class="section-label">Included Modules</h3>
            
            <div class="card mb-lg" style="padding: 8px 20px;">
              <div class="toggle-row">
                <span class="toggle-label">Include Probing Grid Trend</span>
                <label class="toggle-switch">
                  <input type="checkbox" id="rep-ppdTrend" ${AppState.reportConfig.ppdTrend ? 'checked' : ''} onchange="toggleReportConfig('ppdTrend', this.checked)">
                  <div class="toggle-track"></div>
                  <div class="toggle-knob"></div>
                </label>
              </div>
              
              <div class="divider"></div>
              
              <div class="toggle-row">
                <span class="toggle-label">Include BOP percentage summary</span>
                <label class="toggle-switch">
                  <input type="checkbox" id="rep-bopChart" ${AppState.reportConfig.bopChart ? 'checked' : ''} onchange="toggleReportConfig('bopChart', this.checked)">
                  <div class="toggle-track"></div>
                  <div class="toggle-knob"></div>
                </label>
              </div>
              
              <div class="divider"></div>
              
              <div class="toggle-row">
                <span class="toggle-label">Include AI Diagnosis Staging</span>
                <label class="toggle-switch">
                  <input type="checkbox" id="rep-aiProbability" ${AppState.reportConfig.aiProbability ? 'checked' : ''} onchange="toggleReportConfig('aiProbability', this.checked)">
                  <div class="toggle-track"></div>
                  <div class="toggle-knob"></div>
                </label>
              </div>
              
              <div class="divider"></div>
              
              <div class="toggle-row">
                <span class="toggle-label">Clinical Interventions Guidance</span>
                <label class="toggle-switch">
                  <input type="checkbox" id="rep-clinicalVerdict" ${AppState.reportConfig.clinicalVerdict ? 'checked' : ''} onchange="toggleReportConfig('clinicalVerdict', this.checked)">
                  <div class="toggle-track"></div>
                  <div class="toggle-knob"></div>
                </label>
              </div>

              <div class="divider"></div>
              
              <div class="toggle-row">
                <span class="toggle-label">Signature / Stamp Block</span>
                <label class="toggle-switch">
                  <input type="checkbox" id="rep-signatureBlock" ${AppState.reportConfig.signatureBlock ? 'checked' : ''} onchange="toggleReportConfig('signatureBlock', this.checked)">
                  <div class="toggle-track"></div>
                  <div class="toggle-knob"></div>
                </label>
              </div>
            </div>

            <div class="page-actions" style="padding: 24px 0 0;">
              <button class="btn btn-primary" onclick="window.location.hash = '#report-preview'">
                Preview Document
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  `;
}

function renderReportPreview() {
  const dateStr = new Date().toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
  return `
    <div class="app-shell">
      ${renderSidebarMarkup()}
      <div class="main-content">
        ${renderTopBarMarkup('Report Preview')}
        <div class="page">
          <div class="page-header flex justify-between items-center">
            <div>
              <p class="page-subtitle">Verify clinical document</p>
              <h2 class="page-title">Report Preview</h2>
            </div>
            <button class="btn btn-outline btn-sm" onclick="window.print()" style="width: auto;">
              🖨️ Print
            </button>
          </div>
          
          <div class="page-body">
            <!-- Simulated PDF Report Sheet -->
            <div id="printable-report-sheet" class="report-card" style="text-align: left;">
              <div class="flex justify-between items-start" style="margin-bottom: 24px;">
                <div>
                  <h3 style="font-weight: 900; font-size: 20px; color: var(--primary);">POCKET DEPTH AI</h3>
                  <p style="font-size: 11px; color: var(--text-secondary); text-transform: uppercase;">Clinical Periodontal Report</p>
                </div>
                <div style="text-align: right;">
                  <span style="font-size: 12px; font-weight: 700; color: var(--text-primary);">${dateStr}</span>
                </div>
              </div>
              
              <div class="divider"></div>
              
              <h4 style="font-size: 12px; font-weight: 700; color: var(--text-secondary); text-transform: uppercase; margin: 16px 0 8px;">
                1. Patient Profile
              </h4>
              <div class="report-row"><span class="report-label">Name:</span><span class="report-value">${AppState.activePatientName}</span></div>
              <div class="report-row"><span class="report-label">Clinician in Charge:</span><span class="report-value">Dr. ${AppState.clinicalUserName}</span></div>
              <div class="report-row"><span class="report-label">Facility Name:</span><span class="report-value">${AppState.clinicalUserClinic}</span></div>
              
              <div class="divider"></div>
              
              <h4 style="font-size: 12px; font-weight: 700; color: var(--text-secondary); text-transform: uppercase; margin: 16px 0 8px;">
                2. Summary Findings
              </h4>
              <div class="report-row"><span class="report-label">Clinical Mean PPD:</span><span class="report-value">${AppState.meanPpd} mm</span></div>
              <div class="report-row"><span class="report-label">Clinical Mean CAL:</span><span class="report-value">${AppState.meanCal} mm</span></div>
              <div class="report-row"><span class="report-label">BOP Frequency:</span><span class="report-value">${AppState.bopPercentage}%</span></div>
              <div class="report-row"><span class="report-label">Deep Pockets (>=4mm):</span><span class="report-value">${AppState.deepPocketsCount} sites</span></div>
              
              ${AppState.reportConfig.aiProbability ? `
                <div class="divider"></div>
                <h4 style="font-size: 12px; font-weight: 700; color: var(--text-secondary); text-transform: uppercase; margin: 16px 0 8px;">
                  3. Dynamic AI Verdict
                </h4>
                <div style="background: var(--background); padding: 12px; border-radius: 8px; margin-bottom: 12px;">
                  <p style="font-weight: 800; font-size: 15px; color: var(--primary);">${AppState.clinicalVerdict}</p>
                  <p style="font-size: 13px; color: var(--text-secondary); margin-top: 2px;">Grading classification: ${AppState.stageAndGrade}</p>
                </div>
              ` : ''}

              ${AppState.reportConfig.clinicalVerdict ? `
                <div class="divider"></div>
                <h4 style="font-size: 12px; font-weight: 700; color: var(--text-secondary); text-transform: uppercase; margin: 16px 0 8px;">
                  4. Recommended Interventions
                </h4>
                <ul style="list-style-type: none; font-size: 13px; color: #555; padding-left: 0;">
                  <li style="margin-bottom: 6px;">• Subgingival quadrant Scaling & Root Planing (SRP)</li>
                  <li style="margin-bottom: 6px;">• Localized antimicrobial irrigation</li>
                  <li style="margin-bottom: 6px;">• Periodontal hygiene maintenance follow-up (3 months)</li>
                </ul>
              ` : ''}

              ${AppState.reportConfig.signatureBlock ? `
                <div style="margin-top: 48px; display: flex; justify-content: space-between; align-items: flex-end;">
                  <div>
                    <div style="width: 160px; border-bottom: 1.5px solid var(--text-primary);"></div>
                    <p style="font-size: 11px; color: var(--text-secondary); margin-top: 4px; text-transform: uppercase; font-weight: 700;">Authorized Signature</p>
                  </div>
                  <div style="text-align: right;">
                    <p style="font-size: 11px; color: var(--text-secondary); font-weight: 700; text-transform: uppercase;">License Number</p>
                    <p style="font-size: 13px; font-weight: 700; color: var(--text-primary);">${AppState.clinicalUserLicense}</p>
                  </div>
                </div>
              ` : ''}
            </div>

            <div class="page-actions" style="padding: 32px 0 0; flex-direction: column; gap: 12px;">
              <button class="btn btn-primary" onclick="handleDownloadReport()">
                📥 Download PDF Report
              </button>
              <button class="btn btn-outline" onclick="openShareModal()">
                🔗 Share Document
              </button>
              <button class="btn btn-text" onclick="window.location.hash = '#dashboard'">
                Return to Dashboard
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Share Modal Overlay -->
    <div id="share-modal-overlay" class="modal-overlay hidden" onclick="closeShareModal()">
      <div class="modal-card" onclick="event.stopPropagation()">
        <h3 style="font-weight: 800; font-size: 18px; margin-bottom: 12px;">Share Report</h3>
        <p style="color: var(--text-secondary); font-size: 13px;">Disseminate generated periodontal diagnostic values safely.</p>
        
        <div class="share-options">
          <div class="share-option" onclick="simulateShare('Email')">
            <span class="icon">✉️</span>
            <span class="label">Email</span>
          </div>
          <div class="share-option" onclick="simulateShare('WhatsApp')">
            <span class="icon">💬</span>
            <span class="label">WhatsApp</span>
          </div>
          <div class="share-option" onclick="simulateShare('Cloud Vault')">
            <span class="icon">☁️</span>
            <span class="label">Cloud</span>
          </div>
        </div>
        
        <button class="btn btn-outline btn-sm" onclick="closeShareModal()">Close</button>
      </div>
    </div>
  `;
}

function renderReportsArchive() {
  return `
    <div class="app-shell">
      ${renderSidebarMarkup()}
      <div class="main-content">
        ${renderTopBarMarkup('Reports Archive')}
        <div class="page">
          <div class="page-header">
            <h2 class="page-title">Reports Archive</h2>
            <p class="page-subtitle">View and print historical patient probing audits</p>
          </div>
          
          <div class="page-body">
            <div id="reports-archive-list">
              ${AppState.reports.length === 0 
                ? '<p style="text-align: center; color: var(--text-secondary); padding: 32px 0;">No reports compiled yet</p>'
                : AppState.reports.map(r => `
                  <div class="list-item" onclick="viewHistoricalReport('${r.id}')">
                    <div class="avatar avatar-purple">📄</div>
                    <div class="list-info">
                      <h4 class="list-title">${r.patient}</h4>
                      <p class="list-subtitle">${r.verdict} (${r.stage})</p>
                    </div>
                    <div class="list-meta">
                      <div class="list-date">${r.date}</div>
                      <div class="list-status status-active">${r.id}</div>
                    </div>
                  </div>
                `).join('')
              }
            </div>
            
            <button class="btn btn-primary mt-lg" onclick="window.location.hash = '#start-examination'">
              New Examination
            </button>
          </div>
        </div>
      </div>
    </div>
  `;
}

function renderPatientDirectory() {
  return `
    <div class="app-shell">
      ${renderSidebarMarkup()}
      <div class="main-content">
        ${renderTopBarMarkup('Patient Directory')}
        <div class="page">
          <div class="page-header flex justify-between items-center">
            <div>
              <h2 class="page-title">Patient Records</h2>
              <p class="page-subtitle">Manage registered clinical cases</p>
            </div>
            <button class="btn btn-primary btn-sm" onclick="window.location.hash = '#patient-registration'" style="width: auto;">
              ➕ New
            </button>
          </div>
          
          <div class="page-body">
            <div class="form-group">
              <div class="input-wrapper">
                <span class="input-icon">🔍</span>
                <input type="text" id="patient-directory-search" class="input-field" placeholder="Search by name..." oninput="filterPatientDirectoryList()">
              </div>
            </div>
            
            <div id="patient-directory-list" style="margin-top: 20px;">
              ${AppState.patients.map(p => `
                <div class="list-item">
                  <div class="avatar avatar-purple">${p.name.charAt(0)}</div>
                  <div class="list-info">
                    <h4 class="list-title">${p.name}</h4>
                    <p class="list-subtitle">${p.email || 'No email'} | ${p.phone || 'No phone'}</p>
                    <p style="font-size: 11px; color: var(--text-secondary); margin-top: 4px; line-height: 1.3;">
                      ${p.history || 'No notes archived'}
                    </p>
                  </div>
                </div>
              `).join('')}
            </div>
          </div>
        </div>
      </div>
    </div>
  `;
}

function renderUserProfile() {
  return `
    <div class="app-shell">
      ${renderSidebarMarkup()}
      <div class="main-content">
        ${renderTopBarMarkup('UserProfile')}
        <div class="page">
          <div class="page-header">
            <p class="page-subtitle">Account Credentials</p>
            <h2 class="page-title">Clinical Profile</h2>
          </div>
          
          <div class="page-body">
            <div class="text-center" style="margin-bottom: 32px;">
              <div class="avatar avatar-purple avatar-lg" style="margin: 0 auto 16px;">
                ${AppState.clinicalUserName.charAt(0)}
              </div>
              <h3 style="font-weight: 800; font-size: 22px;">Dr. ${AppState.clinicalUserName}</h3>
              <p class="text-gray" style="font-size: 14px; font-weight: 500; margin-top: 2px;">Chief Periodontal Specialist</p>
            </div>

            <div class="card mb-lg">
              <div class="card-body" style="padding: 24px;">
                <div class="profile-info-row">
                  <span class="profile-info-icon">🎖️</span>
                  <div>
                    <p class="profile-info-label">Medical License</p>
                    <p class="profile-info-value">${AppState.clinicalUserLicense}</p>
                  </div>
                </div>
                <div class="divider"></div>
                <div class="profile-info-row">
                  <span class="profile-info-icon">🏥</span>
                  <div>
                    <p class="profile-info-label">Affiliated Clinic</p>
                    <p class="profile-info-value">${AppState.clinicalUserClinic}</p>
                  </div>
                </div>
                <div class="divider"></div>
                <div class="profile-info-row">
                  <span class="profile-info-icon">✉️</span>
                  <div>
                    <p class="profile-info-label">Email Address</p>
                    <p class="profile-info-value">${AppState.clinicalUserEmail}</p>
                  </div>
                </div>
              </div>
            </div>

            <button class="btn btn-primary" onclick="openEditProfileDialog()">
              Edit Clinical Profile
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Edit Profile Dialog Overlay -->
    <div id="edit-profile-overlay" class="dialog-overlay hidden">
      <div class="dialog" style="max-width: 400px;">
        <h3 class="dialog-title">Edit Profile</h3>
        <p class="dialog-body" style="margin-bottom: 16px;">Update your clinical directory card credentials.</p>
        
        <div class="form-group">
          <label class="form-label">FULL NAME</label>
          <input type="text" id="edit-prof-name" class="input-field input-no-icon" value="${AppState.clinicalUserName}">
        </div>
        <div class="form-group">
          <label class="form-label">CLINIC NAME</label>
          <input type="text" id="edit-prof-clinic" class="input-field input-no-icon" value="${AppState.clinicalUserClinic}">
        </div>
        <div class="form-group">
          <label class="form-label">LICENSE NUMBER</label>
          <input type="text" id="edit-prof-license" class="input-field input-no-icon" value="${AppState.clinicalUserLicense}">
        </div>

        <div class="dialog-actions" style="gap: 10px;">
          <button class="btn btn-outline btn-sm" onclick="closeEditProfileDialog()">Cancel</button>
          <button class="btn btn-primary btn-sm" onclick="handleSaveProfileChanges()">Save</button>
        </div>
      </div>
    </div>
  `;
}

function renderSettings() {
  const currentConfigStr = SafeStorage.getItem('pdd_firebase_config');
  let config = { apiKey: '', authDomain: '', projectId: '', appId: '' };
  if (currentConfigStr) {
    try { config = JSON.parse(currentConfigStr); } catch (e) {}
  }

  return `
    <div class="app-shell">
      ${renderSidebarMarkup()}
      <div class="main-content">
        ${renderTopBarMarkup('Settings')}
        <div class="page">
          <div class="page-header">
            <h2 class="page-title">System Settings</h2>
            <p class="page-subtitle">Configure app rules and preferences</p>
          </div>
          
          <div class="page-body">
            <!-- Firebase Integration Panel -->
            <div class="card mb-lg" style="padding: 20px; background: var(--surface); border: 1px solid rgba(255,255,255,0.05); margin-bottom: 24px;">
              <h3 style="font-weight: 800; font-size: 16px; margin-bottom: 8px; display: flex; align-items: center; gap: 8px; color: var(--text-primary);">
                🔥 Firebase Cloud Connection
              </h3>
              <p style="color: var(--text-secondary); font-size: 13px; margin-bottom: 20px; line-height: 1.4;">
                Connect directly to your Firebase web project to enable real-time cloud data storage, authenticated user profiles, and cross-device sync.
              </p>
              
              <div class="form-group" style="margin-bottom: 14px;">
                <label style="display: block; font-size: 11px; font-weight: 700; margin-bottom: 6px; text-transform: uppercase; letter-spacing: 0.5px; color: var(--text-secondary);">API Key</label>
                <input type="text" id="fb-apiKey" class="input-field" value="${config.apiKey || ''}" placeholder="AIzaSy...">
              </div>
              
              <div class="form-group" style="margin-bottom: 14px;">
                <label style="display: block; font-size: 11px; font-weight: 700; margin-bottom: 6px; text-transform: uppercase; letter-spacing: 0.5px; color: var(--text-secondary);">Auth Domain</label>
                <input type="text" id="fb-authDomain" class="input-field" value="${config.authDomain || ''}" placeholder="your-project.firebaseapp.com">
              </div>
              
              <div class="form-group" style="margin-bottom: 14px;">
                <label style="display: block; font-size: 11px; font-weight: 700; margin-bottom: 6px; text-transform: uppercase; letter-spacing: 0.5px; color: var(--text-secondary);">Project ID</label>
                <input type="text" id="fb-projectId" class="input-field" value="${config.projectId || ''}" placeholder="your-project-id">
              </div>
              
              <div class="form-group" style="margin-bottom: 20px;">
                <label style="display: block; font-size: 11px; font-weight: 700; margin-bottom: 6px; text-transform: uppercase; letter-spacing: 0.5px; color: var(--text-secondary);">App ID</label>
                <input type="text" id="fb-appId" class="input-field" value="${config.appId || ''}" placeholder="1:12345:web:abcd...">
              </div>

              <div style="display: flex; gap: 12px;">
                <button class="btn btn-primary" onclick="handleSaveFirebaseConfig()" style="width: auto; padding: 10px 20px; font-size: 13px;">
                  💾 Save Configuration
                </button>
                ${currentConfigStr ? `
                  <button class="btn btn-outline" onclick="handleClearFirebaseConfig()" style="width: auto; padding: 10px 20px; font-size: 13px; color: #FF5252; border-color: #FF5252;">
                    🗑️ Disconnect
                  </button>
                ` : ''}
              </div>
            </div>

            <!-- Standard Switches -->
            <div class="card mb-lg" style="padding: 8px 20px; margin-bottom: 24px;">
              <div class="toggle-row">
                <span class="toggle-label">Push Notifications</span>
                <label class="toggle-switch">
                  <input type="checkbox" checked>
                  <div class="toggle-track"></div>
                  <div class="toggle-knob"></div>
                </label>
              </div>
              
              <div class="divider"></div>
              
              <div class="toggle-row">
                <span class="toggle-label">Cloud Backup Sync</span>
                <label class="toggle-switch">
                  <input type="checkbox" checked>
                  <div class="toggle-track"></div>
                  <div class="toggle-knob"></div>
                </label>
              </div>
              
              <div class="divider"></div>
              
              <div class="toggle-row">
                <span class="toggle-label">Biometric Keypad Lock</span>
                <label class="toggle-switch">
                  <input type="checkbox">
                  <div class="toggle-track"></div>
                  <div class="toggle-knob"></div>
                </label>
              </div>
              
              <div class="divider"></div>
              
              <div class="toggle-row">
                <span class="toggle-label">Auto-Generate PDF Summary</span>
                <label class="toggle-switch">
                  <input type="checkbox" checked>
                  <div class="toggle-track"></div>
                  <div class="toggle-knob"></div>
                </label>
              </div>
            </div>

            <button class="btn btn-outline" onclick="window.location.hash = '#dashboard'">
              Return to Previous Screen
            </button>
          </div>
        </div>
      </div>
    </div>
  `;
}

function renderFilterDashboard() {
  return `
    <div class="app-shell">
      ${renderSidebarMarkup()}
      <div class="main-content">
        ${renderTopBarMarkup('Filter Cases')}
        <div class="page">
          <div class="page-header">
            <h2 class="page-title">Filter Cases</h2>
            <p class="page-subtitle">Configure what metrics reflect on your charts</p>
          </div>
          
          <div class="page-body">
            <h3 class="section-label">Categorize by Verdict</h3>
            
            <div class="card mb-lg" style="padding: 12px 20px;">
              <div class="form-group" style="margin: 8px 0;">
                <label class="toggle-row" style="padding: 8px 0;">
                  <span class="toggle-label">All Active Cases</span>
                  <input type="radio" name="dash-filter" value="All Cases" ${AppState.dashboardFilter === 'All Cases' ? 'checked' : ''} onchange="setDashboardFilter('All Cases')">
                </label>
              </div>
              <div class="divider"></div>
              <div class="form-group" style="margin: 8px 0;">
                <label class="toggle-row" style="padding: 8px 0;">
                  <span class="toggle-label">Healthy Patients Baseline</span>
                  <input type="radio" name="dash-filter" value="Healthy Cases" ${AppState.dashboardFilter === 'Healthy Cases' ? 'checked' : ''} onchange="setDashboardFilter('Healthy Cases')">
                </label>
              </div>
              <div class="divider"></div>
              <div class="form-group" style="margin: 8px 0;">
                <label class="toggle-row" style="padding: 8px 0;">
                  <span class="toggle-label">Periodontitis Severe/Moderate</span>
                  <input type="radio" name="dash-filter" value="Periodontitis Cases" ${AppState.dashboardFilter === 'Periodontitis Cases' ? 'checked' : ''} onchange="setDashboardFilter('Periodontitis Cases')">
                </label>
              </div>
            </div>

            <button class="btn btn-primary" onclick="window.location.hash = '#dashboard'">
              Apply Filter
            </button>
          </div>
        </div>
      </div>
    </div>
  `;
}


// --- Helper UI Markups ---

function renderSidebarMarkup() {
  const currentHash = window.location.hash || '#dashboard';
  return `
    <div id="sidebar-overlay" class="sidebar-overlay" onclick="toggleSidebar(false)"></div>
    <div id="app-sidebar" class="sidebar">
      <div class="sidebar-header">
        <div class="sidebar-logo">🦷</div>
        <div class="sidebar-brand">
          <h2>Pocket Depth AI</h2>
          <p>Clinical Edition</p>
        </div>
      </div>
      
      <div class="sidebar-divider"></div>
      
      <div class="sidebar-nav">
        <button class="sidebar-item ${currentHash === '#dashboard' ? 'active' : ''}" onclick="window.location.hash = '#dashboard'">
          <span class="icon">📊</span> Main Dashboard
        </button>
        <button class="sidebar-item ${currentHash === '#patient-registration' ? 'active' : ''}" onclick="window.location.hash = '#patient-registration'">
          <span class="icon">👤</span> Register Patient
        </button>
        <button class="sidebar-item ${currentHash === '#notifications' ? 'active' : ''}" onclick="window.location.hash = '#notifications'">
          <span class="icon">🔔</span> Notifications
        </button>
        <button class="sidebar-item ${currentHash === '#patient-directory' ? 'active' : ''}" onclick="window.location.hash = '#patient-directory'">
          <span class="icon">🔍</span> Search Patients
        </button>
        <button class="sidebar-item ${currentHash === '#filter-dashboard' ? 'active' : ''}" onclick="window.location.hash = '#filter-dashboard'">
          <span class="icon">⚡</span> Filter Dashboard
        </button>
        <button class="sidebar-item ${currentHash === '#reports-archive' ? 'active' : ''}" onclick="window.location.hash = '#reports-archive'">
          <span class="icon">📄</span> Reports
        </button>
        <button class="sidebar-item ${currentHash.startsWith('#manual-input') ? 'active' : ''}" onclick="window.location.hash = '#manual-input/Patient'">
          <span class="icon">✏️</span> Manual Input
        </button>
        
        <div class="sidebar-divider"></div>
        
        <button class="sidebar-item ${currentHash === '#user-profile' ? 'active' : ''}" onclick="window.location.hash = '#user-profile'">
          <span class="icon">👤</span> User Profile
        </button>
        <button class="sidebar-item ${currentHash === '#settings' ? 'active' : ''}" onclick="window.location.hash = '#settings'">
          <span class="icon">⚙️</span> Settings
        </button>
        <button class="sidebar-item" onclick="handleLogout()" style="color: var(--error);">
          <span class="icon">🚪</span> Logout
        </button>
      </div>
    </div>
  `;
}

function renderTopBarMarkup(title) {
  return `
    <div class="top-bar">
      <button class="icon-btn menu-toggle" onclick="toggleSidebar(true)" style="color: var(--primary);">
        ☰
      </button>
      <div class="top-bar-title">${title}</div>
      <div class="top-bar-actions">
        <button class="icon-btn" onclick="window.location.hash = '#notifications'">
          🔔
          <span class="notification-badge"></span>
        </button>
      </div>
    </div>
  `;
}

// --- Handler Actions & Functions ---

function toggleSidebar(open) {
  const sidebar = document.getElementById('app-sidebar');
  const overlay = document.getElementById('sidebar-overlay');
  if (sidebar && overlay) {
    if (open) {
      sidebar.classList.add('open');
      overlay.classList.add('active');
    } else {
      sidebar.classList.remove('open');
      overlay.classList.remove('active');
    }
  }
}

function updateActiveSidebarItem(hash) {
  const items = document.querySelectorAll('.sidebar-item');
  items.forEach(item => {
    item.classList.remove('active');
    // Check match
    const onclickStr = item.getAttribute('onclick') || '';
    if (onclickStr.includes(hash)) {
      item.classList.add('active');
    }
  });
}

function handleLogin(e) {
  e.preventDefault();
  const email = document.getElementById('login-email').value;
  const pass = document.getElementById('login-password').value;
  const errDiv = document.getElementById('login-error');

  if (errDiv) errDiv.classList.add('hidden');

  ApiService.loginUser(email, pass).then(res => {
    if (res.success) {
      AppState.clinicalUserName = res.name;
      AppState.isAuthenticated = true;
      SafeStorage.setItem('pdd_is_authenticated', 'true');
      window.location.hash = '#dashboard';
    } else {
      errDiv.textContent = res.message || 'Login failed.';
      errDiv.classList.remove('hidden');
    }
  }).catch(err => {
    errDiv.textContent = err.message || 'Login failed. Please verify credentials or run in Offline Mode.';
    errDiv.classList.remove('hidden');
  });
}

function handleSignup(e) {
  e.preventDefault();
  const name = document.getElementById('signup-name').value;
  const email = document.getElementById('signup-email').value;
  const phone = document.getElementById('signup-phone').value;
  const pass = document.getElementById('signup-password').value;
  const errDiv = document.getElementById('signup-error');

  if (errDiv) errDiv.classList.add('hidden');

  ApiService.registerUser(name, email, phone, pass).then(res => {
    if (res.success) {
      AppState.clinicalUserName = name;
      AppState.isAuthenticated = true;
      SafeStorage.setItem('pdd_is_authenticated', 'true');
      // Save details locally
      SafeStorage.setItem('pdd_registered_user', JSON.stringify({ name, email, phone }));
      window.location.hash = '#dashboard';
    } else {
      errDiv.textContent = res.message || 'Signup failed.';
      errDiv.classList.remove('hidden');
    }
  }).catch(err => {
    errDiv.textContent = err.message || 'Signup failed. Please verify credentials or run in Offline Mode.';
    errDiv.classList.remove('hidden');
  });
}

function handleForgotPassword(e) {
  e.preventDefault();
  window.location.hash = '#recovery-success';
}

function handlePatientRegistration(e) {
  e.preventDefault();
  const name = document.getElementById('patient-reg-name').value;
  const email = document.getElementById('patient-reg-email').value;
  const phone = document.getElementById('patient-reg-phone').value;
  const history = document.getElementById('patient-reg-history').value;

  AppState.patients.push({ name, email, phone, history });
  SafeStorage.setItem('pdd_patients', JSON.stringify(AppState.patients));

  AppState.activePatientName = name;

  // Clear current clinical probing values
  clearAllProbingDataInternal();

  ApiService.registerPatient(name, email, phone, history).then(() => {
    window.location.hash = '#registration-success';
  }).catch(err => {
    console.warn('[Sync failed] Patient registered locally:', err);
    window.location.hash = '#registration-success';
  });
}

function handleOfflineBypass() {
  SafeStorage.setItem('pdd_firebase_enabled', 'false');
  ApiService.reinitializeFirebase();
  AppState.clinicalUserName = 'Sarah Johnson';
  AppState.isAuthenticated = true;
  SafeStorage.setItem('pdd_is_authenticated', 'true');
  window.location.hash = '#dashboard';
}

function handleStartPatientExam(encodedName) {
  const name = decodeURIComponent(encodedName);
  AppState.activePatientName = name;
  clearAllProbingDataInternal();
  window.location.hash = `#examination-instructions/${encodedName}`;
}

function setDashboardFilter(val) {
  AppState.dashboardFilter = val;
}

// --- Retrain AI Engine ---
function handleRetrainAI() {
  if (AppState.isTrainingModel) return;
  AppState.isTrainingModel = true;
  AppState.aiTrainingStatus = 'Training model on XAMPP MySQL...';
  router(); // Refresh view

  ApiService.trainModel().then(res => {
    AppState.isTrainingModel = false;
    if (res.success) {
      AppState.aiModelAccuracy = res.accuracy;
      AppState.isCustomModelTrained = true;
      AppState.aiTrainingStatus = `Model trained! Accuracy: ${res.accuracy}%`;
      showDialog("Clinical AI Vault", `Model retrained successfully on XAMPP MySQL database!\nOptimized Accuracy: ${res.accuracy}%`);
      
      // Update metrics locally based on new weights
      recalculateMetrics();
    }
  }).catch(err => {
    AppState.isTrainingModel = false;
    AppState.aiTrainingStatus = 'Training failed';
    showDialog("Clinical AI Vault", `AI Training Failed:\n${err.message}`);
    router();
  });
}

// --- Save Case Training Sample ---
function handleSaveCaseToVault() {
  ApiService.addTrainingSample(
    AppState.meanPpd,
    AppState.meanCal,
    AppState.bopPercentage,
    AppState.deepPocketsCount,
    AppState.clinicalVerdict
  ).then(res => {
    if (res.success) {
      showDialog("Clinical AI Vault", "Successfully contributed this case to the XAMPP MySQL database!\nThis sample will be used to make future predictions more precise.");
    }
  }).catch(err => {
    showDialog("Clinical AI Vault", `Database Error:\n${err.message}`);
  });
}

// --- Profile Page Toggles ---
function openEditProfileDialog() {
  document.getElementById('edit-profile-overlay').classList.remove('hidden');
}
function closeEditProfileDialog() {
  document.getElementById('edit-profile-overlay').classList.add('hidden');
}
function handleSaveProfileChanges() {
  const name = document.getElementById('edit-prof-name').value;
  const clinic = document.getElementById('edit-prof-clinic').value;
  const license = document.getElementById('edit-prof-license').value;

  AppState.clinicalUserName = name;
  AppState.clinicalUserClinic = clinic;
  AppState.clinicalUserLicense = license;

  closeEditProfileDialog();
  router();
}

// --- Firebase configuration handlers ---
function handleSaveFirebaseConfig() {
  const apiKey = document.getElementById('fb-apiKey').value.trim();
  const authDomain = document.getElementById('fb-authDomain').value.trim();
  const projectId = document.getElementById('fb-projectId').value.trim();
  const appId = document.getElementById('fb-appId').value.trim();

  if (!apiKey || !authDomain || !projectId || !appId) {
    showDialog("Setup Error", "All fields are required to establish a connection with Firebase.");
    return;
  }

  const newConfig = { apiKey, authDomain, projectId, appId };
  SafeStorage.setItem('pdd_firebase_config', JSON.stringify(newConfig));
  SafeStorage.setItem('pdd_firebase_enabled', 'true');

  const connected = ApiService.reinitializeFirebase();
  if (connected) {
    showDialog("Firebase Connected", "System successfully connected to Google Firebase Cloud Services.");
  } else {
    showDialog("Connection Failed", "Credentials saved, but initialization failed. Check credentials formatting and project status.");
  }
  router();
}

function handleClearFirebaseConfig() {
  SafeStorage.removeItem('pdd_firebase_config');
  SafeStorage.setItem('pdd_firebase_enabled', 'false');
  ApiService.reinitializeFirebase();
  showDialog("Firebase Disconnected", "Credentials cleared. The application is now running in local Offline Mock Mode.");
  router();
}

// --- Login / Sign Up Page Firebase configuration handlers ---
function toggleLoginFirebaseConfig(prefix = 'login') {
  const container = document.getElementById(`${prefix}-fb-config-container`);
  if (!container) return;
  
  const isHidden = container.classList.toggle('hidden');
  if (!isHidden) {
    // Populate fields from current SafeStorage
    const currentConfigStr = SafeStorage.getItem('pdd_firebase_config');
    let config = { apiKey: '', authDomain: '', projectId: '', appId: '' };
    if (currentConfigStr) {
      try { config = JSON.parse(currentConfigStr); } catch (e) {}
    }
    
    const isEnabled = SafeStorage.getItem('pdd_firebase_enabled') !== 'false';
    
    document.getElementById(`${prefix}-fb-apiKey`).value = config.apiKey || '';
    document.getElementById(`${prefix}-fb-authDomain`).value = config.authDomain || '';
    document.getElementById(`${prefix}-fb-projectId`).value = config.projectId || '';
    document.getElementById(`${prefix}-fb-appId`).value = config.appId || '';
    document.getElementById(`${prefix}-fb-enabled`).checked = isEnabled;
  }
}

function handleSaveLoginFirebaseConfig(prefix = 'login') {
  const apiKey = document.getElementById(`${prefix}-fb-apiKey`).value.trim();
  const authDomain = document.getElementById(`${prefix}-fb-authDomain`).value.trim();
  const projectId = document.getElementById(`${prefix}-fb-projectId`).value.trim();
  const appId = document.getElementById(`${prefix}-fb-appId`).value.trim();
  const enabled = document.getElementById(`${prefix}-fb-enabled`).checked;

  if (enabled && (!apiKey || !authDomain || !projectId || !appId)) {
    showDialog("Setup Error", "All fields are required to enable connection with Firebase Cloud Services.");
    return;
  }

  const newConfig = { apiKey, authDomain, projectId, appId };
  SafeStorage.setItem('pdd_firebase_config', JSON.stringify(newConfig));
  SafeStorage.setItem('pdd_firebase_enabled', enabled ? 'true' : 'false');

  const connected = ApiService.reinitializeFirebase();
  if (enabled) {
    if (connected) {
      showDialog("Firebase Connected", "System successfully connected to Google Firebase Cloud Services.");
      const container = document.getElementById(`${prefix}-fb-config-container`);
      if (container) container.classList.add('hidden');
    } else {
      showDialog("Connection Failed", "Credentials saved, but initialization failed. Check credentials formatting and project status.");
    }
  } else {
    showDialog("Firebase Disabled", "Cloud synchronization disabled. App will run in Offline Mock Mode.");
    const container = document.getElementById(`${prefix}-fb-config-container`);
    if (container) container.classList.add('hidden');
  }
}

function handleLogout() {
  AppState.isAuthenticated = false;
  SafeStorage.removeItem('pdd_is_authenticated');
  window.location.hash = '#login';
}

// --- Integrated Manual Grid Voice Recognition ---
function toggleManualVoiceListening(patientName) {
  const decoded = decodeURIComponent(patientName);
  const statusSpan = document.getElementById('manual-voice-listening-status');
  const btn = document.getElementById('btn-manual-mic-toggle');
  const parseCard = document.getElementById('manual-voice-parse-card');
  const recognizedSpan = document.getElementById('manual-voice-recognized-text');

  if (AppState.isListening) {
    VoiceService.stop();
    AppState.isListening = false;
    if (statusSpan) statusSpan.textContent = 'Mic Standby — Tap to speak measurements';
    if (btn) btn.className = 'mic-btn mic-btn-idle';
    updateManualVoiceWaveform(false);
    updateVoiceWaveform(false);
  } else {
    if (statusSpan) statusSpan.textContent = 'Listening... Speak tooth number and 3 depths';
    if (btn) btn.className = 'mic-btn mic-btn-listening';
    updateManualVoiceWaveform(true);
    updateVoiceWaveform(true);

    VoiceService.start(
      (text) => {
        if (recognizedSpan) recognizedSpan.innerHTML = `"${text}"`;
        if (parseCard) parseCard.classList.remove('hidden');

        const parsed = Parser.parseSpeech(text);
        if (parsed) {
          if (recognizedSpan) {
            recognizedSpan.innerHTML = `"${text}"<br><strong style="color: #4CAF50; font-size: 14px;">Logged Tooth ${parsed.toothNumber}: ${parsed.values.join(' - ')} mm</strong>`;
          }
          
          // Save entry
          const entry = {
            toothNumber: parsed.toothNumber,
            values: parsed.values,
            timestamp: Date.now()
          };
          AppState.allToothData.push(entry);
          AppState.voiceLog.unshift(entry);

          // Update main state for tooth values
          parsed.values.forEach((v, idx) => {
            AppState.manualPpd[`tooth${parsed.toothNumber}_ppd_${idx}`] = v.toString();
            // Dynamically update DOM cells
            const cell = document.getElementById(`cell-tooth${parsed.toothNumber}_ppd_${idx}`);
            if (cell) {
              cell.textContent = v.toString();
              cell.classList.add('filled');
            }
          });

          // Sync to XAMPP asynchronously
          ApiService.saveToothData(decoded, parsed.toothNumber, parsed.values, entry.timestamp);
          
          saveProbingDataToStorage();
        } else {
          if (recognizedSpan) {
            recognizedSpan.innerHTML = `"${text}"<br><span style="color: #FF5252; font-size: 11px;">Could not parse. Try: "Tooth 11 three four five"</span>`;
          }
        }
      },
      (err) => {
        if (statusSpan) statusSpan.textContent = 'Mic Error';
        if (btn) btn.className = 'mic-btn mic-btn-no-perm';
        updateManualVoiceWaveform(false);
        updateVoiceWaveform(false);
        AppState.isListening = false;
        if (recognizedSpan) recognizedSpan.textContent = `Hardware error: ${err}`;
      }
    );
    AppState.isListening = true;
  }
}

function updateManualVoiceWaveform(active) {
  const ring1 = document.getElementById('manual-mic-pulse-1');
  const ring2 = document.getElementById('manual-mic-pulse-2');
  if (ring1 && ring2) {
    if (active) {
      ring1.style.animation = 'mic-pulse 1.8s infinite ease-in-out';
      ring2.style.animation = 'mic-pulse 1.8s infinite ease-in-out';
      ring2.style.animationDelay = '0.9s';
    } else {
      ring1.style.animation = 'none';
      ring2.style.animation = 'none';
    }
  }
}

function simulateManualVoiceInput(patientName) {
  const decoded = decodeURIComponent(patientName);
  const examples = [
    "Tooth eleven three four five",
    "Tooth three two two three",
    "Tooth fourteen five six five",
    "Tooth twenty one four four four",
    "Tooth eight three three four",
    "Tooth thirty two six five seven"
  ];
  const phrase = examples[Math.floor(Math.random() * examples.length)];
  
  if (AppState.isListening) {
    VoiceService.stop();
    AppState.isListening = false;
    const statusSpan = document.getElementById('manual-voice-listening-status');
    const btn = document.getElementById('btn-manual-mic-toggle');
    if (statusSpan) statusSpan.textContent = 'Mic Standby';
    if (btn) btn.className = 'mic-btn mic-btn-idle';
    updateManualVoiceWaveform(false);
    updateVoiceWaveform(false);
  }

  const parseCard = document.getElementById('manual-voice-parse-card');
  const recognizedSpan = document.getElementById('manual-voice-recognized-text');
  if (recognizedSpan) recognizedSpan.innerHTML = `Simulating speech: "${phrase}"`;
  if (parseCard) parseCard.classList.remove('hidden');

  setTimeout(() => {
    const parsed = Parser.parseSpeech(phrase);
    if (parsed) {
      if (recognizedSpan) {
        recognizedSpan.innerHTML = `"${phrase}"<br><strong style="color: #4CAF50; font-size: 14px;">Logged Tooth ${parsed.toothNumber}: ${parsed.values.join(' - ')} mm</strong>`;
      }
      
      const entry = {
        toothNumber: parsed.toothNumber,
        values: parsed.values,
        timestamp: Date.now()
      };
      AppState.allToothData.push(entry);
      AppState.voiceLog.unshift(entry);

      // Save to state
      parsed.values.forEach((v, idx) => {
        AppState.manualPpd[`tooth${parsed.toothNumber}_ppd_${idx}`] = v.toString();
        // Dynamically update DOM cells
        const cell = document.getElementById(`cell-tooth${parsed.toothNumber}_ppd_${idx}`);
        if (cell) {
          cell.textContent = v.toString();
          cell.classList.add('filled');
        }
      });

      // Sync to backend
      ApiService.saveToothData(decoded, parsed.toothNumber, parsed.values, entry.timestamp);
      
      saveProbingDataToStorage();
    }
  }, 800);
}

// --- Voice Recognition Logic ---
function toggleVoiceListening(patientName) {
  const decoded = decodeURIComponent(patientName);
  const statusSpan = document.getElementById('voice-listening-status');
  const btn = document.getElementById('btn-mic-toggle');

  if (AppState.isListening) {
    // Stop recognition
    VoiceService.stop();
    AppState.isListening = false;
    statusSpan.textContent = 'Mic Standby';
    btn.className = 'mic-btn mic-btn-idle';
    updateVoiceWaveform(false);
  } else {
    // Start recognition
    statusSpan.textContent = 'Listening...';
    btn.className = 'mic-btn mic-btn-listening';
    updateVoiceWaveform(true);

    VoiceService.start(
      (text) => {
        // Result callback
        AppState.recognizedText = text;
        const resultCard = document.getElementById('voice-parse-card');
        const textSpan = document.getElementById('voice-recognized-text');
        textSpan.innerHTML = `"${text}"`;

        const parsed = Parser.parseSpeech(text);
        if (parsed) {
          resultCard.className = 'voice-result-card voice-result-success';
          textSpan.innerHTML += `<br><strong style="font-size: 15px;">Logged Tooth ${parsed.toothNumber}: ${parsed.values.join(' - ')} mm</strong>`;
          
          // Save entry
          const entry = {
            toothNumber: parsed.toothNumber,
            values: parsed.values,
            timestamp: Date.now()
          };
          AppState.allToothData.push(entry);
          AppState.voiceLog.unshift(entry);

          // Update main state for tooth values
          parsed.values.forEach((v, idx) => {
            AppState.manualPpd[`tooth${parsed.toothNumber}_ppd_${idx}`] = v.toString();
          });

          // Sync to XAMPP asynchronously
          ApiService.saveToothData(decoded, parsed.toothNumber, parsed.values, entry.timestamp);

          // Save to storage
          saveProbingDataToStorage();

          // Re-render session list
          renderVoiceSessionList();
        } else {
          resultCard.className = 'voice-result-card voice-result-error';
          textSpan.innerHTML += `<br><span style="font-size: 12px; font-weight: 700;">Could not parse format. Try: "Tooth 11 three four five"</span>`;
        }
      },
      (err) => {
        // Error callback
        statusSpan.textContent = 'Mic Error';
        btn.className = 'mic-btn mic-btn-no-perm';
        updateVoiceWaveform(false);
        AppState.isListening = false;
        document.getElementById('voice-recognized-text').textContent = `Permission / hardware error: ${err}`;
      }
    );
    AppState.isListening = true;
  }
}

function updateVoiceWaveform(active) {
  const bars = document.querySelectorAll('.wave-bar');
  bars.forEach(bar => {
    if (active) {
      bar.style.animation = `wave-anim 1.2s ease-in-out infinite`;
      bar.style.animationDelay = `${Math.random() * 0.5}s`;
      bar.classList.add('active');
    } else {
      bar.style.animation = 'none';
      bar.classList.remove('active');
    }
  });
}

function renderVoiceSessionList() {
  const container = document.getElementById('voice-session-log-list');
  if (!container) return;
  
  const header = document.getElementById('voice-log-header');
  if (header) {
    header.textContent = `Session Measurements Log (${AppState.voiceLog.length})`;
  }

  if (AppState.voiceLog.length === 0) {
    container.innerHTML = '<p style="font-size: 13px; opacity: 0.6; text-align: center; padding: 12px 0;">No sites logged yet in this session</p>';
    return;
  }
  container.innerHTML = AppState.voiceLog.map(v => {
    const maxVal = Math.max(...v.values);
    let severity = 'Healthy';
    let chipBg = 'var(--success-bg)';
    let chipColor = 'var(--success)';
    if (maxVal >= 6) { severity = 'Severe'; chipBg = 'var(--error-bg)'; chipColor = 'var(--error)'; }
    else if (maxVal >= 4) { severity = 'Moderate'; chipBg = 'var(--warning-bg)'; chipColor = 'var(--warning)'; }

    return `
      <div class="voice-entry-inner">
        <div class="tooth-badge">${v.toothNumber}</div>
        <div style="font-weight: 700; font-size: 14px;">Values: ${v.values.join(' - ')} mm</div>
        <div class="depth-badge" style="background: ${chipBg}; color: ${chipColor};">
          ${severity}
        </div>
      </div>
    `;
  }).join('');
}

function simulateVoiceInput(patientName) {
  // Simulate voice input strings
  const examples = [
    "Tooth eleven three four five",
    "Tooth three two two three",
    "Tooth fourteen five six five",
    "Tooth twenty one four four four",
    "Tooth eight three three four",
    "Tooth thirty two six five seven"
  ];
  const phrase = examples[Math.floor(Math.random() * examples.length)];
  
  if (AppState.isListening) {
    VoiceService.stop();
    AppState.isListening = false;
    document.getElementById('voice-listening-status').textContent = 'Mic Standby';
    document.getElementById('btn-mic-toggle').className = 'mic-btn mic-btn-idle';
    updateVoiceWaveform(false);
  }

  const resultCard = document.getElementById('voice-parse-card');
  const textSpan = document.getElementById('voice-recognized-text');
  textSpan.innerHTML = `Simulating speech: "${phrase}"`;

  setTimeout(() => {
    const parsed = Parser.parseSpeech(phrase);
    if (parsed) {
      resultCard.className = 'voice-result-card voice-result-success';
      textSpan.innerHTML = `"${phrase}"<br><strong style="font-size: 15px;">Logged Tooth ${parsed.toothNumber}: ${parsed.values.join(' - ')} mm</strong>`;
      
      const entry = {
        toothNumber: parsed.toothNumber,
        values: parsed.values,
        timestamp: Date.now()
      };
      AppState.allToothData.push(entry);
      AppState.voiceLog.unshift(entry);

      // Save to state
      parsed.values.forEach((v, idx) => {
        AppState.manualPpd[`tooth${parsed.toothNumber}_ppd_${idx}`] = v.toString();
      });

      // Sync to backend
      ApiService.saveToothData(decodeURIComponent(patientName), parsed.toothNumber, parsed.values, entry.timestamp);
      
      saveProbingDataToStorage();
      
      renderVoiceSessionList();
    }
  }, 800);
}

function handleVoiceFinish() {
  try {
    VoiceService.stop();
  } catch (e) {
    console.error('[Voice] Stop failed:', e);
  }
  AppState.isListening = false;
  recalculateMetrics()
    .catch((err) => {
      console.error('[Recalculate] Error recalculating metrics:', err);
    })
    .finally(() => {
      window.location.hash = '#processing-examination';
    });
}

// --- Probing Grid Keypad Selection ---
let currentFocusedCellId = null;

function focusManualCell(cellId) {
  currentFocusedCellId = cellId;
  
  // Highlight cell
  const cells = document.querySelectorAll('.ppd-cell');
  cells.forEach(c => c.classList.remove('focused'));

  const activeCell = document.getElementById(`cell-${cellId}`);
  if (activeCell) {
    activeCell.classList.add('focused');
  }

  // Update keypad header
  const label = cellId.includes('ppd') ? 'POCKET DEPTH (PPD)' : 'ATTACHMENT LOSS (CAL)';
  const num = cellId.split('_')[0].replace('tooth', '');
  const siteIndex = cellId.split('_')[2];
  const sites = ['Distobuccal', 'Buccal', 'Mesiobuccal'];
  
  document.getElementById('keypad-focused-cell-label').textContent = `Tooth ${num} - ${sites[siteIndex]} (${label})`;

  // Slide keypad open
  document.getElementById('manual-clinical-keypad').classList.add('open');
}

function closeKeypad() {
  document.getElementById('manual-clinical-keypad').classList.remove('open');
  const activeCell = document.getElementById(`cell-${currentFocusedCellId}`);
  if (activeCell) {
    activeCell.classList.remove('focused');
  }
}

function pressKeypadValue(val) {
  if (!currentFocusedCellId) return;

  const cell = document.getElementById(`cell-${currentFocusedCellId}`);
  const isPpd = currentFocusedCellId.includes('ppd');

  if (val === 'del') {
    if (isPpd) {
      delete AppState.manualPpd[currentFocusedCellId];
    } else {
      delete AppState.manualCal[currentFocusedCellId];
    }
    cell.textContent = '-';
    cell.classList.remove('filled');
  } else {
    if (isPpd) {
      AppState.manualPpd[currentFocusedCellId] = val.toString();
    } else {
      AppState.manualCal[currentFocusedCellId] = val.toString();
    }
    cell.textContent = val;
    cell.classList.add('filled');
  }
  saveProbingDataToStorage();
}

function toggleManualBop(toothNum, checked) {
  AppState.manualBop[toothNum] = checked;
  saveProbingDataToStorage();
}

function clearAllProbingData() {
  if (confirm("Are you sure you want to clear all probing entries on this grid?")) {
    clearAllProbingDataInternal();
    router();
  }
}

function clearAllProbingDataInternal() {
  AppState.manualPpd = {};
  AppState.manualCal = {};
  AppState.manualBop = {};
  AppState.voiceLog = [];
  AppState.allToothData = [];

  const storage = window.SafeStorage || (typeof SafeStorage !== 'undefined' ? SafeStorage : null);
  if (storage) {
    storage.removeItem('pdd_manual_ppd');
    storage.removeItem('pdd_manual_cal');
    storage.removeItem('pdd_manual_bop');
    storage.removeItem('pdd_voice_log');
    storage.removeItem('pdd_all_tooth_data');
  }
}

// --- Recalculate Periodontal Metrics ---
async function recalculateMetrics() {
  try {
    const metrics = AnalyticsEngine.computeMetrics(AppState);
    
    AppState.meanPpd = metrics.meanPpd;
    AppState.meanCal = metrics.meanCal;
    AppState.bopPercentage = metrics.bopPercentage;
    AppState.deepPocketsCount = metrics.deepPocketsCount;
    AppState.clinicalVerdict = metrics.clinicalVerdict;
    AppState.stageAndGrade = metrics.stageAndGrade;
    AppState.probHealthy = metrics.probHealthy;
    AppState.probModerate = metrics.probModerate;
    AppState.probSevere = metrics.probSevere;

    // Make backend API request to update predicted weights if possible
    try {
      const res = await ApiService.predictDisease(
        metrics.meanPpd,
        metrics.meanCal,
        metrics.bopPercentage,
        metrics.deepPocketsCount
      );
      if (res && res.success) {
        AppState.clinicalVerdict = res.verdict;
        AppState.stageAndGrade = res.stage_and_grade;
        AppState.probHealthy = res.prob_healthy;
        AppState.probModerate = res.prob_moderate;
        AppState.probSevere = res.prob_severe;
      }
    } catch (e) {
      console.log('[Recalculate] Offline mode prediction retained.', e);
    }
  } catch (err) {
    console.error('[Recalculate] General error computing metrics:', err);
  }
}

// --- Toggle Report Configuration Toggles ---
function toggleReportConfig(key, checked) {
  AppState.reportConfig[key] = checked;
}

// --- Build a self-contained report element with all styles inlined (no CSS vars) ---
function buildStandaloneReportElement() {
  const dateStr = new Date().toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
  const primaryColor = '#5B4FCF';
  const textPrimary = '#1A1A2E';
  const textSecondary = '#6B7280';
  const bgColor = '#F5F4FB';
  const dividerColor = '#E5E7EB';

  const clinicianName = AppState.clinicalUserName.trim().toLowerCase().startsWith('dr.') 
    ? AppState.clinicalUserName.trim() 
    : `Dr. ${AppState.clinicalUserName.trim()}`;

  const aiVerdictSection = `
    <div style="margin-top:18px;">
      <h4 style="font-size:11px;font-weight:700;color:${textSecondary};text-transform:uppercase;letter-spacing:1px;margin:0 0 8px;">3. Dynamic AI Verdict</h4>
      <div style="background:${bgColor};padding:14px;border-radius:8px;border-left:4px solid ${primaryColor};">
        <p style="font-weight:800;font-size:16px;color:${primaryColor};margin:0 0 4px;">${AppState.clinicalVerdict}</p>
        <p style="font-size:13px;color:${textSecondary};margin:0;">Grading classification: ${AppState.stageAndGrade}</p>
      </div>
    </div>`;

  const interventionsSection = `
    <div style="margin-top:18px;">
      <h4 style="font-size:11px;font-weight:700;color:${textSecondary};text-transform:uppercase;letter-spacing:1px;margin:0 0 8px;">4. Recommended Interventions</h4>
      <ul style="list-style:none;padding:0;margin:0;font-size:13px;color:#444;">
        <li style="margin-bottom:6px;">• Subgingival quadrant Scaling &amp; Root Planing (SRP)</li>
        <li style="margin-bottom:6px;">• Localized antimicrobial irrigation</li>
        <li style="margin-bottom:6px;">• Periodontal hygiene maintenance follow-up (3 months)</li>
      </ul>
    </div>`;

  const signatureSection = `
    <div style="margin-top:48px;display:flex;justify-content:space-between;align-items:flex-end;border-top:1px solid ${dividerColor};padding-top:24px;">
      <div>
        <div style="width:160px;border-bottom:1.5px solid ${textPrimary};margin-bottom:4px;"></div>
        <p style="font-size:11px;color:${textSecondary};text-transform:uppercase;font-weight:700;margin:0;">Authorized Signature</p>
      </div>
      <div style="text-align:right;">
        <p style="font-size:11px;color:${textSecondary};font-weight:700;text-transform:uppercase;margin:0 0 2px;">License Number</p>
        <p style="font-size:13px;font-weight:700;color:${textPrimary};margin:0;">${AppState.clinicalUserLicense}</p>
      </div>
    </div>`;

  const html = `
    <div style="
      font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
      background: #ffffff;
      color: ${textPrimary};
      padding: 40px;
      width: 700px;
      box-sizing: border-box;
    ">
      <!-- Header -->
      <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:8px;">
        <div>
          <h3 style="font-size:22px;font-weight:900;color:${primaryColor};margin:0 0 2px;">POCKET DEPTH AI</h3>
          <p style="font-size:11px;color:${textSecondary};text-transform:uppercase;letter-spacing:1px;margin:0;">Clinical Periodontal Report</p>
        </div>
        <span style="font-size:13px;font-weight:700;color:${textPrimary};">${dateStr}</span>
      </div>

      <!-- Divider -->
      <hr style="border:none;border-top:1px solid ${dividerColor};margin:16px 0;">

      <!-- 1. Patient Profile -->
      <h4 style="font-size:11px;font-weight:700;color:${textSecondary};text-transform:uppercase;letter-spacing:1px;margin:0 0 10px;">1. Patient Profile</h4>
      <table style="width:100%;font-size:14px;border-collapse:collapse;">
        <tr><td style="padding:6px 0;color:${textSecondary};">Name:</td><td style="padding:6px 0;font-weight:700;text-align:right;">${AppState.activePatientName}</td></tr>
        <tr><td style="padding:6px 0;color:${textSecondary};">Clinician in Charge:</td><td style="padding:6px 0;font-weight:700;text-align:right;">${clinicianName}</td></tr>
        <tr><td style="padding:6px 0;color:${textSecondary};">Facility Name:</td><td style="padding:6px 0;font-weight:700;text-align:right;">${AppState.clinicalUserClinic}</td></tr>
      </table>

      <!-- Divider -->
      <hr style="border:none;border-top:1px solid ${dividerColor};margin:16px 0;">

      <!-- 2. Summary Findings -->
      <h4 style="font-size:11px;font-weight:700;color:${textSecondary};text-transform:uppercase;letter-spacing:1px;margin:0 0 10px;">2. Summary Findings</h4>
      <table style="width:100%;font-size:14px;border-collapse:collapse;">
        <tr><td style="padding:6px 0;color:${textSecondary};">Clinical Mean PPD:</td><td style="padding:6px 0;font-weight:700;text-align:right;">${AppState.meanPpd} mm</td></tr>
        <tr><td style="padding:6px 0;color:${textSecondary};">Clinical Mean CAL:</td><td style="padding:6px 0;font-weight:700;text-align:right;">${AppState.meanCal} mm</td></tr>
        <tr><td style="padding:6px 0;color:${textSecondary};">BOP Frequency:</td><td style="padding:6px 0;font-weight:700;text-align:right;">${AppState.bopPercentage}%</td></tr>
        <tr><td style="padding:6px 0;color:${textSecondary};">Deep Pockets (&ge;4mm):</td><td style="padding:6px 0;font-weight:700;text-align:right;">${AppState.deepPocketsCount} sites</td></tr>
      </table>

      ${AppState.reportConfig.aiProbability ? aiVerdictSection : ''}
      ${AppState.reportConfig.clinicalVerdict ? interventionsSection : ''}
      ${AppState.reportConfig.signatureBlock ? signatureSection : ''}

      <!-- Footer -->
      <hr style="border:none;border-top:1px solid ${dividerColor};margin:32px 0 12px;">
      <p style="font-size:10px;color:${textSecondary};text-align:center;margin:0;">
        Generated by Pocket Depth AI — Clinical Edition &nbsp;|&nbsp; ${dateStr}
      </p>
    </div>`;

  const wrapper = document.createElement('div');
  wrapper.innerHTML = html;
  return wrapper.firstElementChild;
}

// --- Download real PDF Report that matches the preview ---
function handleDownloadReport() {
  const dateStr = new Date().toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
  const reportObj = {
    id: `R-${Math.floor(1000 + Math.random() * 9000)}`,
    patient: AppState.activePatientName,
    date: dateStr,
    verdict: AppState.clinicalVerdict,
    stage: AppState.stageAndGrade,
    ppd: AppState.meanPpd,
    bop: AppState.bopPercentage
  };
  AppState.reports.unshift(reportObj);

  const reportEl = buildStandaloneReportElement();
  // Place element fixed top/left 0 so html2canvas computes positive bounds, 
  // but keep it hidden behind app layer using z-index
  reportEl.style.position = 'fixed';
  reportEl.style.top = '0';
  reportEl.style.left = '0';
  reportEl.style.zIndex = '-9999';
  reportEl.style.width = '700px';
  reportEl.style.backgroundColor = '#ffffff';
  document.body.appendChild(reportEl);

  const opt = {
    margin:       [10, 10, 10, 10],
    filename:     `Periodontal_Report_${AppState.activePatientName.replace(/\s+/g, '_')}.pdf`,
    image:        { type: 'jpeg', quality: 0.98 },
    html2canvas:  { scale: 2, useCORS: true, logging: false },
    jsPDF:        { unit: 'mm', format: 'a4', orientation: 'portrait' }
  };

  html2pdf().set(opt).from(reportEl).save().then(() => {
    document.body.removeChild(reportEl);
    showDialog("Report Downloaded", "The periodontal PDF report has been downloaded successfully.");
    window.location.hash = '#reports-archive';
  }).catch((err) => {
    console.error('PDF generation failed', err);
    document.body.removeChild(reportEl);
    showDialog("PDF Error", "Could not generate PDF. Please try again.");
  });
}

// --- Share Modal actions ---
function openShareModal() {
  document.getElementById('share-modal-overlay').classList.remove('hidden');
}
function closeShareModal() {
  document.getElementById('share-modal-overlay').classList.add('hidden');
}
function simulateShare(platform) {
  closeShareModal();

  const reportEl = buildStandaloneReportElement();
  reportEl.style.position = 'fixed';
  reportEl.style.top = '0';
  reportEl.style.left = '0';
  reportEl.style.zIndex = '-9999';
  reportEl.style.width = '700px';
  reportEl.style.backgroundColor = '#ffffff';
  document.body.appendChild(reportEl);

  const filename = `Periodontal_Report_${AppState.activePatientName.replace(/\s+/g, '_')}.pdf`;
  const opt = {
    margin:       [10, 10, 10, 10],
    filename:     filename,
    image:        { type: 'jpeg', quality: 0.98 },
    html2canvas:  { scale: 2, useCORS: true, logging: false },
    jsPDF:        { unit: 'mm', format: 'a4', orientation: 'portrait' }
  };

  html2pdf().set(opt).from(reportEl).output('blob').then(async (pdfBlob) => {
    document.body.removeChild(reportEl);
    const file = new File([pdfBlob], filename, { type: 'application/pdf' });

    if (navigator.canShare && navigator.canShare({ files: [file] })) {
      try {
        await navigator.share({
          title: 'Periodontal Report',
          text: `Clinical Report for ${AppState.activePatientName}`,
          files: [file]
        });
        showDialog("Report Shared", `The PDF report was shared via ${platform} successfully.`);
      } catch (error) {
        if (error.name !== 'AbortError') {
          console.error('Sharing failed', error);
          showDialog("Sharing Failed", "Sharing was cancelled or failed.");
        }
      }
    } else {
      // Fallback: download so user can attach manually
      const url = URL.createObjectURL(pdfBlob);
      const a = document.createElement('a');
      a.href = url;
      a.download = filename;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      setTimeout(() => URL.revokeObjectURL(url), 1000);
      showDialog("PDF Downloaded", `PDF saved! Open it and share via ${platform} directly — your browser doesn't support auto-sharing from web apps.`);
    }
  }).catch((error) => {
    document.body.removeChild(reportEl);
    console.error('PDF generation failed', error);
    showDialog("PDF Error", "There was an error generating the PDF report. Please try again.");
  });
}

function resetExaminationState() {
  clearAllProbingDataInternal();
  window.location.hash = '#start-examination';
}

// --- Dialog / Message Alert Helper ---
function showDialog(title, body) {
  const existingOverlay = document.getElementById('global-dialog-overlay');
  if (existingOverlay) existingOverlay.remove();

  const overlay = document.createElement('div');
  overlay.id = 'global-dialog-overlay';
  overlay.className = 'dialog-overlay';
  overlay.innerHTML = `
    <div class="dialog">
      <h3 class="dialog-title">${title}</h3>
      <p class="dialog-body">${body.replace(/\n/g, '<br>')}</p>
      <div class="dialog-actions">
        <button class="btn btn-primary btn-sm" style="width: auto;" onclick="document.getElementById('global-dialog-overlay').remove()">OK</button>
      </div>
    </div>
  `;
  document.body.appendChild(overlay);
}

// --- Search / Filter Lists ---
function filterStartExaminationList() {
  const query = document.getElementById('patient-search-input').value.toLowerCase();
  const items = document.querySelectorAll('#start-examination-list .list-item');
  items.forEach(item => {
    const text = item.querySelector('.list-title').textContent.toLowerCase();
    if (text.includes(query)) {
      item.style.display = 'flex';
    } else {
      item.style.display = 'none';
    }
  });
}

function filterPatientDirectoryList() {
  const query = document.getElementById('patient-directory-search').value.toLowerCase();
  const items = document.querySelectorAll('#patient-directory-list .list-item');
  items.forEach(item => {
    const text = item.querySelector('.list-title').textContent.toLowerCase();
    if (text.includes(query)) {
      item.style.display = 'flex';
    } else {
      item.style.display = 'none';
    }
  });
}

// --- Run startup ---
window.onload = initApp;

function saveProbingDataToStorage() {
  const storage = window.SafeStorage || (typeof SafeStorage !== 'undefined' ? SafeStorage : null);
  if (!storage) return;
  storage.setItem('pdd_manual_ppd', JSON.stringify(AppState.manualPpd));
  storage.setItem('pdd_manual_cal', JSON.stringify(AppState.manualCal));
  storage.setItem('pdd_manual_bop', JSON.stringify(AppState.manualBop));
  storage.setItem('pdd_voice_log', JSON.stringify(AppState.voiceLog));
  storage.setItem('pdd_all_tooth_data', JSON.stringify(AppState.allToothData));
}

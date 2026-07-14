package com.periodontal.ai.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Login : Screen("login")
    data object Signup : Screen("signup")
    data object Dashboard : Screen("dashboard")
    data object PatientRegistration : Screen("patient_registration")
    data object RegistrationSuccess : Screen("registration_success")
    data object StartExamination : Screen("start_examination")
    
    // Parameterized routes
    data object ExaminationInstructions : Screen("examination_instructions/{patientName}") {
        fun createRoute(patientName: String) = "examination_instructions/$patientName"
    }
    data object VoiceInput : Screen("voice_input/{patientName}") {
        fun createRoute(patientName: String) = "voice_input/$patientName"
    }
    data object ManualInput : Screen("manual_input/{patientName}") {
        fun createRoute(patientName: String) = "manual_input/$patientName"
    }
    
    data object ProcessingExamination : Screen("processing_examination")
    data object StatisticsSummary : Screen("statistics_summary")
    data object AIClassification : Screen("ai_classification")
    data object ProbabilityBreakdown : Screen("probability_breakdown")
    data object GenerateReport : Screen("generate_report")
    data object ReportPreview : Screen("report_preview")
    data object ShareReport : Screen("share_report")
    data object DownloadReport : Screen("download_report")
    data object ReportsList : Screen("reports_list")
    data object UserProfile : Screen("user_profile")
    data object Settings : Screen("settings")
    data object Notifications : Screen("notifications")
    data object SearchPatients : Screen("search_patients")
    data object FilterDashboard : Screen("filter_dashboard")
    data object ForgotPassword : Screen("forgot_password")
    data object Success : Screen("success")

    // Legacy/Subfolder app compatibility
    data object PatientsList : Screen("patients")
    data object PatientDetails : Screen("patient_details")
    data object ProbingSession : Screen("probing")
    data object ReportDetail : Screen("report")
}

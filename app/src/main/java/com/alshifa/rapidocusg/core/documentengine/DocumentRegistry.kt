package com.alshifa.rapidocusg.core.documentengine

data class DocumentMeta(
    val type: DocumentType,
    val displayName: String,
    val formRoute: String,
    val previewRoute: String
)

object DocumentRegistry {
    val docs = listOf(
        DocumentMeta(DocumentType.USG_ABDOMEN, "USG Abdomen", "doc/usg_abdomen/form", "doc/usg_abdomen/preview"),
        DocumentMeta(DocumentType.MEDICAL_CERTIFICATE, "Medical Certificate", "doc/medical_certificate/form", "doc/medical_certificate/preview"),
        DocumentMeta(DocumentType.PRESCRIPTION, "Prescription", "doc/prescription/form", "doc/prescription/preview"),
        DocumentMeta(DocumentType.LAB_REQUEST_SLIP, "Lab Request Slip", "doc/lab_request_slip/form", "doc/lab_request_slip/preview"),
        DocumentMeta(DocumentType.RADIOLOGY_REQUEST_SLIP, "Radiology Request Slip", "doc/radiology_request_slip/form", "doc/radiology_request_slip/preview")
    )

    fun byType(type: DocumentType): DocumentMeta = docs.first { it.type == type }
    fun byRoute(route: String?): DocumentMeta? = docs.firstOrNull { it.formRoute == route || it.previewRoute == route }

    fun allowedFor(plan: PlanTier, type: DocumentType): Boolean {
        return when (plan) {
            PlanTier.FREE -> type == DocumentType.USG_ABDOMEN || type == DocumentType.MEDICAL_CERTIFICATE
            PlanTier.PRO -> true
        }
    }
}

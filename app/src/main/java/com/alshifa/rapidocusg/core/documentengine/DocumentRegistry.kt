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
        DocumentMeta(DocumentType.USG_KUB, "USG KUB / Renal Tract", "doc/usg_kub/form", "doc/usg_kub/preview"),
        DocumentMeta(DocumentType.USG_PELVIS, "USG Pelvis (Female)", "doc/usg_pelvis/form", "doc/usg_pelvis/preview"),
        DocumentMeta(DocumentType.USG_OBSTETRIC, "USG Obstetric", "doc/usg_obstetric/form", "doc/usg_obstetric/preview"),
        DocumentMeta(DocumentType.MEDICAL_LEAVE_CERT, "Medical Leave Certificate", "doc/medical_leave_cert/form", "doc/medical_leave_cert/preview"),
        DocumentMeta(DocumentType.MEDICAL_FITNESS_CERT, "Medical Fitness Certificate", "doc/medical_fitness_cert/form", "doc/medical_fitness_cert/preview")
    )

    fun byType(type: DocumentType): DocumentMeta = docs.first { it.type == type }
    fun byRoute(route: String?): DocumentMeta? = docs.firstOrNull { it.formRoute == route || it.previewRoute == route }
}

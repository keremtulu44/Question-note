package com.keremtulu.questionnote.domain.model

/**
 * Product decision: teacher workspace is parked, not deleted. See AGENTS.md.
 * Do not enable or develop it without a new explicit user request.
 * Student questions-to-ask-the-teacher remain part of the active student product.
 */
object UrunKapsami {
    const val OGRETMEN_MODU_GORUNUR = false

    /** Stored role is left untouched so hiding a workspace never destroys user preferences/data. */
    fun gorunenRol(kayitliRol: KullaniciRolu): KullaniciRolu =
        if (OGRETMEN_MODU_GORUNUR) kayitliRol else KullaniciRolu.OGRENCI
}

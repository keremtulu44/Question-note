package com.keremtulu.questionnote.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class UrunKapsamiTest {
    @Test fun teacherWorkspaceStaysHiddenUntilExplicitProductDecision() {
        assertFalse(UrunKapsami.OGRETMEN_MODU_GORUNUR)
    }

    @Test fun previouslySavedTeacherRoleRoutesToStudentWithoutRewritingIt() {
        val kayitliRol = KullaniciRolu.OGRETMEN
        assertEquals(KullaniciRolu.OGRENCI, UrunKapsami.gorunenRol(kayitliRol))
        assertEquals(KullaniciRolu.OGRETMEN, kayitliRol)
    }

    @Test fun studentRoleRemainsStudent() {
        assertEquals(KullaniciRolu.OGRENCI, UrunKapsami.gorunenRol(KullaniciRolu.OGRENCI))
    }
}

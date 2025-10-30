/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package supplier;

/**
 * Simple shared context untuk menyimpan ID supplier yang sedang diedit.
 * Digunakan agar datasupplier bisa set id saat Edit diklik, dan editdatasupplier
 * akan membaca id tersebut saat panel ditampilkan.
 */
public class SupplierContext {
    public static Integer editingId = null;
}

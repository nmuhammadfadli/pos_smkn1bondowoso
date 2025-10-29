/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uiresponsive;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.icepdf.ri.common.ComponentKeyBinding;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;
import page.dashboard;
import barang.databarang;
import barang.tambahdatabarang;
import barang.tambahdetailbarang;
import barang.editdatabarang;
import supplier.datasupplier;
import supplier.tambahdatasupplier;
import supplier.editdatasupplier;
import guru.dataguru;
import guru.tambahdataguru;
import guru.editdataguru;
import pengguna.datapengguna;
import pengguna.tambahdatapengguna;
import pengguna.editdatapengguna;
import hutang.datahutang;
import transaksi_penjualan.transaksipenjualan;
import transaksi_pembelian.transaksipembelian;
import kategori.datakategori;
import laporan.laporanpenjualan;
import laporan.laporanpembelian;
import voucher.datavoucher;
import voucher.editdatavoucher;
import voucher.tambahdatavoucher;
/**
 *
 * @author ThinkPad
 */
public class Mainmenu extends javax.swing.JFrame {
boolean a = false;
private javax.swing.JButton activeButton = null;
private tambahdatabarang tambahDataPanel;
private dashboard dashboardPanel;
private databarang databarangPanel;
private tambahdetailbarang tambahDetailPanel;
private editdatabarang editDataBarangPanel;
private datasupplier datasupplierPanel;
private tambahdatasupplier tambahdatasupplierPanel;
private editdatasupplier editdatasupplierPanel;
private dataguru datagurupanel;
private tambahdataguru tambahdatagurupanel;
private editdataguru editdatagurupanel;
private datapengguna datapenggunapanel;
private tambahdatapengguna tambahdatapenggunapanel;
private editdatapengguna editdatapenggunapanel;
private datahutang datahutangpanel;
private transaksipenjualan transaksipenjualanpanel;
private transaksipembelian transaksipembelianpanel;
private datakategori datakategoripanel;
private laporanpenjualan datalaporanpenjualanpanel;
private laporanpembelian datalaporanpembelianpanel;
private datavoucher datavoucherpanel;
private tambahdatavoucher tambahdatavoucherpanel;
private editdatavoucher editdatavoucherpanel;
  
public Mainmenu() {
    initComponents();
    setUndecorated(true);

    // ====== DAFTAR TOMBOL YANG DIGUNAKAN SEKARANG ======
    javax.swing.JButton[] buttons = {
        dashboardbtn, databarangbtn, datakategoribtn,datasupplierbtn, datagurubtn, datapenggunabtn, datahutangbtn, transaksipenjualanbtn, transaksipembelianbtn, laporanpenjualanbtn, laporanpembelianbtn, voucherbtn,keluarbtn
    };

    // ====== SET ICON DEFAULT ======
    setButtonIcon(dashboardbtn, "dashboard.png");
    setButtonIcon(databarangbtn, "databarang.png");
    setButtonIcon(datakategoribtn, "datakategori.png");
    setButtonIcon(datasupplierbtn, "datasupplier.png");
    setButtonIcon(datagurubtn, "dataguru.png");
    setButtonIcon(datapenggunabtn, "datapengguna.png");
    setButtonIcon(datahutangbtn, "datahutang.png");
    setButtonIcon(transaksipenjualanbtn, "transaksipenjualan.png");
    setButtonIcon(transaksipembelianbtn, "transaksipembelian.png");
    setButtonIcon(laporanpenjualanbtn, "laporanpenjualan.png");
    setButtonIcon(laporanpembelianbtn, "laporanpembelian.png");   
    setButtonIcon(voucherbtn, "voucher.png");
    setButtonIcon(keluarbtn, "keluar.png");
    
    // ====== SET POSISI TEKS, JARAK IKON, DAN STYLE ======
    for (javax.swing.JButton btn : buttons) {
        btn.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btn.setIconTextGap(15);
        styleSidebarButton(btn);
    }

    // ====== INISIASI PANEL ======
    dashboardPanel = new dashboard();
    databarangPanel = new databarang();
    tambahDataPanel = new tambahdatabarang();
    editDataBarangPanel = new editdatabarang();
    tambahDetailPanel = new tambahdetailbarang();
    editdatasupplierPanel = new editdatasupplier();
    datasupplierPanel = new datasupplier();
    tambahdatasupplierPanel = new tambahdatasupplier();
    datagurupanel = new dataguru();
    tambahdatagurupanel = new tambahdataguru();
    editdatagurupanel = new editdataguru();
    datapenggunapanel= new datapengguna();
    tambahdatapenggunapanel = new tambahdatapengguna();
    editdatapenggunapanel = new editdatapengguna();
    datahutangpanel = new datahutang();
    transaksipenjualanpanel = new transaksipenjualan();
    transaksipembelianpanel = new transaksipembelian();
    datakategoripanel = new datakategori();
    datalaporanpenjualanpanel = new laporanpenjualan();
    datalaporanpembelianpanel = new laporanpembelian();
    datavoucherpanel = new datavoucher();
    tambahdatavoucherpanel = new tambahdatavoucher();
    editdatavoucherpanel = new editdatavoucher();
    
    // ====== PANEL DEFAULT ======
        setActiveButton(dashboardbtn);
    page.removeAll();
    page.add(dashboardPanel);
    page.revalidate();
    page.repaint();
}

private void styleSidebarButton(final javax.swing.JButton btn) {
    btn.setFocusPainted(false);
    btn.setBorderPainted(false);
    btn.setContentAreaFilled(true);
    btn.setOpaque(true);
    btn.setBackground(new Color(245, 245, 245));
    if (btn == transaksipenjualanbtn || btn == transaksipembelianbtn || btn == laporanpembelianbtn) {
        btn.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 14));
    } else {
        btn.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 14));
    }
    btn.setForeground(new Color(45, 45, 45));
    btn.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    btn.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 20, 10, 10));
    btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

    btn.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            // hover: override visual sementara
            btn.setBackground(new Color(236, 28, 44));
            btn.setForeground(Color.WHITE);
            String name = getButtonName(btn);
            if (name != null) {
                setButtonIcon(btn, name + "hover.png");
            }
        }

        @Override
        public void mouseExited(java.awt.event.MouseEvent e) {
            // hanya reset kalau bukan tombol aktif
            if (activeButton == btn) {
                // tombol aktif -> pastikan tetap terlihat aktif
                btn.setBackground(new Color(236, 28, 44));
                btn.setForeground(Color.WHITE);
                String name = getButtonName(btn);
                if (name != null) {
                    setButtonIcon(btn, name + "hover.png");
                }
            } else {
                // tombol tidak aktif -> kembalikan normal
                btn.setBackground(new Color(245, 245, 245));
                btn.setForeground(new Color(45, 45, 45));
                String name = getButtonName(btn);
                if (name != null) {
                    setButtonIcon(btn, name + ".png");
                }
            }
        }
    });
}

private void setActiveButton(javax.swing.JButton btn) {
    // Kembalikan warna tombol sebelumnya
    if (activeButton != null) {
        activeButton.setBackground(new Color(245, 245, 245));
        activeButton.setForeground(new Color(45, 45, 45));
        String prevName = getButtonName(activeButton);
        if (prevName != null) {
            setButtonIcon(activeButton, prevName + ".png");
        }
    }

    // Set tombol yang diklik jadi aktif
    activeButton = btn;
    btn.setBackground(new Color(236, 28, 44));
    btn.setForeground(Color.WHITE);

    String name = getButtonName(btn);
    if (name != null) {
        setButtonIcon(btn, name + "hover.png");
    }
}
// ===== HELPER UNTUK ICON =====
private void setButtonIcon(javax.swing.JButton btn, String fileName) {
    btn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icon/" + fileName)));
}

private String getButtonName(javax.swing.JButton btn) {
    if (btn == dashboardbtn) return "dashboard";
    if (btn == databarangbtn) return "databarang";
    if (btn == datakategoribtn) return "datakategori";
    if (btn == datasupplierbtn) return "datasupplier";
    if (btn == datagurubtn) return "dataguru";
    if (btn == datapenggunabtn) return "datapengguna";
    if (btn == datahutangbtn) return "datahutang";
    if (btn == transaksipenjualanbtn) return "transaksipenjualan";
    if (btn == transaksipembelianbtn) return "transaksipembelian";
    if (btn == laporanpenjualanbtn) return "laporanpenjualan";
    if (btn == laporanpembelianbtn) return "laporanpembelian";
    if (btn == voucherbtn) return "voucher";
    if (btn == keluarbtn) return "keluar";
    // kalau mau aktifkan tombol lain nanti, tinggal tambahin di sini
    return null;
}
//barang
public void showDataBarangPanel() {
    page.removeAll();
    page.add(databarangPanel);
    page.revalidate();
    page.repaint();
}
public void showTambahDataBarang() {
    page.removeAll();
    page.add(tambahDataPanel);
    page.revalidate();
    page.repaint();
}
public void showTambahDetailBarang() {
    page.removeAll();
    page.add(tambahDetailPanel);
    page.revalidate();
    page.repaint();
}
public void showEditDataBarang() {
    page.removeAll();
    page.add(editDataBarangPanel);
    page.revalidate();
    page.repaint();
}

public void  showdatakategori(){
    page.removeAll();
    page.add(datakategoripanel);
    page.revalidate();
    page.repaint(); 
}

//supplier
public void showDataSupplier() {
    page.removeAll();
    page.add(datasupplierPanel);
    page.revalidate();
    page.repaint();
}
public void showTambahDataSupplier() {
    page.removeAll();
    page.add(tambahdatasupplierPanel);
    page.revalidate();
    page.repaint();
}
 public void showEditDataSupplier() {
    page.removeAll();
    page.add(editdatasupplierPanel);
    page.revalidate();
    page.repaint();
}
 
 //Guru
 public void showDataGuru() {
    page.removeAll();
    page.add(datagurupanel);
    page.revalidate();
    page.repaint();
}
public void showTambahDataGuru() {
    page.removeAll();
    page.add(tambahdatagurupanel);
    page.revalidate();
    page.repaint();
}
 public void showEditDataGuru() {
    page.removeAll();
    page.add(editdatagurupanel);
    page.revalidate();
    page.repaint();
} 
 // Pengguna
 public void showDataPengguna() {
    page.removeAll();
    page.add(datapenggunapanel);
    page.revalidate();
    page.repaint();
}
public void showTambahDataPengguna() {
    page.removeAll();
    page.add(tambahdatapenggunapanel);
    page.revalidate();
    page.repaint();
}
 public void showEditDataPengguna() {
    page.removeAll();
    page.add(editdatapenggunapanel);
    page.revalidate();
    page.repaint();
} 
//hutang
 public void showDataHutang() {
    page.removeAll();
    page.add(datahutangpanel);
    page.revalidate();
    page.repaint();
}


//voucher
 public void showDataVoucher() {
    page.removeAll();
    page.add(datavoucherpanel);
    page.revalidate();
    page.repaint();
}
public void showTambahDataVoucher() {
    page.removeAll();
    page.add(tambahdatavoucherpanel);
    page.revalidate();
    page.repaint();
}
 public void showEditDataVoucher() {
    page.removeAll();
    page.add(editdatavoucherpanel);
    page.revalidate();
    page.repaint();
} 

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Header = new javax.swing.JPanel();
        iconminmaxclose = new javax.swing.JPanel();
        Buttonclose = new javax.swing.JPanel();
        close = new javax.swing.JLabel();
        Buttonmax = new javax.swing.JPanel();
        fullmax = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        menu = new javax.swing.JPanel();
        MenuIcon = new javax.swing.JPanel();
        lineseting = new javax.swing.JPanel();
        setting = new javax.swing.JPanel();
        Buttonsetting = new javax.swing.JLabel();
        hidemenu = new javax.swing.JPanel();
        buttonhidemenu = new javax.swing.JLabel();
        linehidemenu = new javax.swing.JPanel();
        menuhide = new javax.swing.JPanel();
        dashboardbtn = new javax.swing.JButton();
        databarangbtn = new javax.swing.JButton();
        datapenggunabtn = new javax.swing.JButton();
        datagurubtn = new javax.swing.JButton();
        datasupplierbtn = new javax.swing.JButton();
        transaksipenjualanbtn = new javax.swing.JButton();
        datahutangbtn = new javax.swing.JButton();
        keluarbtn = new javax.swing.JButton();
        transaksipembelianbtn = new javax.swing.JButton();
        laporanpenjualanbtn = new javax.swing.JButton();
        laporanpembelianbtn = new javax.swing.JButton();
        voucherbtn = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        datakategoribtn = new javax.swing.JButton();
        page = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);

        Header.setBackground(new java.awt.Color(250, 250, 250));
        Header.setPreferredSize(new java.awt.Dimension(800, 50));
        Header.setLayout(new java.awt.BorderLayout());

        iconminmaxclose.setBackground(new java.awt.Color(250, 250, 250));
        iconminmaxclose.setPreferredSize(new java.awt.Dimension(150, 50));
        iconminmaxclose.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        Buttonclose.setBackground(new java.awt.Color(250, 250, 250));
        Buttonclose.setLayout(new java.awt.BorderLayout());

        close.setForeground(new java.awt.Color(230, 57, 0));
        close.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        close.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icon/exit.png"))); // NOI18N
        close.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                closeMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                closeMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                closeMouseExited(evt);
            }
        });
        Buttonclose.add(close, java.awt.BorderLayout.CENTER);

        iconminmaxclose.add(Buttonclose, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 0, 50, 50));

        Buttonmax.setBackground(new java.awt.Color(250, 250, 250));
        Buttonmax.setLayout(new java.awt.BorderLayout());

        fullmax.setBackground(new java.awt.Color(250, 250, 250));
        fullmax.setForeground(new java.awt.Color(250, 250, 250));
        fullmax.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        fullmax.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icon/max.png"))); // NOI18N
        fullmax.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fullmaxMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                fullmaxMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                fullmaxMouseExited(evt);
            }
        });
        Buttonmax.add(fullmax, java.awt.BorderLayout.CENTER);

        iconminmaxclose.add(Buttonmax, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 0, 50, 50));

        Header.add(iconminmaxclose, java.awt.BorderLayout.LINE_END);

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel1.setText("   SMKN 1 BONDOWOSO");
        Header.add(jLabel1, java.awt.BorderLayout.CENTER);

        getContentPane().add(Header, java.awt.BorderLayout.PAGE_START);

        menu.setPreferredSize(new java.awt.Dimension(50, 450));
        menu.setLayout(new java.awt.BorderLayout());

        MenuIcon.setBackground(new java.awt.Color(250, 250, 250));
        MenuIcon.setPreferredSize(new java.awt.Dimension(50, 450));
        MenuIcon.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lineseting.setBackground(new java.awt.Color(250, 250, 250));
        lineseting.setPreferredSize(new java.awt.Dimension(50, 5));

        javax.swing.GroupLayout linesetingLayout = new javax.swing.GroupLayout(lineseting);
        lineseting.setLayout(linesetingLayout);
        linesetingLayout.setHorizontalGroup(
            linesetingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 50, Short.MAX_VALUE)
        );
        linesetingLayout.setVerticalGroup(
            linesetingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 5, Short.MAX_VALUE)
        );

        MenuIcon.add(lineseting, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 55, 50, 5));

        setting.setBackground(new java.awt.Color(250, 250, 250));
        setting.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                settingMouseEntered(evt);
            }
        });
        setting.setLayout(new java.awt.BorderLayout());

        Buttonsetting.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Buttonsetting.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icon/setting.png"))); // NOI18N
        Buttonsetting.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        Buttonsetting.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ButtonsettingMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                ButtonsettingMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                ButtonsettingMouseExited(evt);
            }
        });
        setting.add(Buttonsetting, java.awt.BorderLayout.CENTER);

        MenuIcon.add(setting, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 55, 50, 50));

        hidemenu.setBackground(new java.awt.Color(250, 250, 250));
        hidemenu.setLayout(new java.awt.BorderLayout());

        buttonhidemenu.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        buttonhidemenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icon/menu_32px.png"))); // NOI18N
        buttonhidemenu.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonhidemenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                buttonhidemenuMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                buttonhidemenuMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                buttonhidemenuMouseExited(evt);
            }
        });
        hidemenu.add(buttonhidemenu, java.awt.BorderLayout.CENTER);

        MenuIcon.add(hidemenu, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 5, 50, 50));

        linehidemenu.setBackground(new java.awt.Color(250, 250, 250));
        linehidemenu.setPreferredSize(new java.awt.Dimension(50, 5));

        javax.swing.GroupLayout linehidemenuLayout = new javax.swing.GroupLayout(linehidemenu);
        linehidemenu.setLayout(linehidemenuLayout);
        linehidemenuLayout.setHorizontalGroup(
            linehidemenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 50, Short.MAX_VALUE)
        );
        linehidemenuLayout.setVerticalGroup(
            linehidemenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 5, Short.MAX_VALUE)
        );

        MenuIcon.add(linehidemenu, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 50, 5));

        menu.add(MenuIcon, java.awt.BorderLayout.LINE_START);

        menuhide.setBackground(new java.awt.Color(246, 246, 246));
        menuhide.setMaximumSize(new java.awt.Dimension(160, 250));
        menuhide.setPreferredSize(new java.awt.Dimension(160, 220));
        menuhide.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        dashboardbtn.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        dashboardbtn.setForeground(new java.awt.Color(255, 255, 255));
        dashboardbtn.setText("Dashboard");
        dashboardbtn.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        dashboardbtn.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        dashboardbtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dashboardbtnActionPerformed(evt);
            }
        });
        menuhide.add(dashboardbtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 100, 220, 40));

        databarangbtn.setText("Data Barang");
        databarangbtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                databarangbtnActionPerformed(evt);
            }
        });
        menuhide.add(databarangbtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 140, 220, 40));

        datapenggunabtn.setText("Data Pengguna");
        datapenggunabtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                datapenggunabtnActionPerformed(evt);
            }
        });
        menuhide.add(datapenggunabtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 300, 220, 40));

        datagurubtn.setText("Data Guru");
        datagurubtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                datagurubtnActionPerformed(evt);
            }
        });
        menuhide.add(datagurubtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 260, 220, 40));

        datasupplierbtn.setText("Data Supplier");
        datasupplierbtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                datasupplierbtnActionPerformed(evt);
            }
        });
        menuhide.add(datasupplierbtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 220, 220, 40));

        transaksipenjualanbtn.setText("Transaksi Penjualan");
        transaksipenjualanbtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transaksipenjualanbtnActionPerformed(evt);
            }
        });
        menuhide.add(transaksipenjualanbtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 380, 220, 40));

        datahutangbtn.setText("Data Hutang");
        datahutangbtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                datahutangbtnActionPerformed(evt);
            }
        });
        menuhide.add(datahutangbtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 340, 220, 40));

        keluarbtn.setText("Keluar");
        keluarbtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keluarbtnActionPerformed(evt);
            }
        });
        menuhide.add(keluarbtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 580, 220, 40));

        transaksipembelianbtn.setText("Transaksi Pembelian");
        transaksipembelianbtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transaksipembelianbtnActionPerformed(evt);
            }
        });
        menuhide.add(transaksipembelianbtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 420, 220, 40));

        laporanpenjualanbtn.setText("Laporan Penjualan");
        laporanpenjualanbtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                laporanpenjualanbtnActionPerformed(evt);
            }
        });
        menuhide.add(laporanpenjualanbtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 460, 220, 40));

        laporanpembelianbtn.setText("Laporan Pembelian");
        laporanpembelianbtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                laporanpembelianbtnActionPerformed(evt);
            }
        });
        menuhide.add(laporanpembelianbtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 500, 220, 40));

        voucherbtn.setText("Voucher");
        voucherbtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                voucherbtnActionPerformed(evt);
            }
        });
        menuhide.add(voucherbtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 540, 220, 40));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/welcome.png"))); // NOI18N
        menuhide.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 180, 72));

        datakategoribtn.setText("Data Kategori");
        datakategoribtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                datakategoribtnActionPerformed(evt);
            }
        });
        menuhide.add(datakategoribtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 180, 220, 40));

        menu.add(menuhide, java.awt.BorderLayout.CENTER);

        getContentPane().add(menu, java.awt.BorderLayout.LINE_START);

        page.setBackground(new java.awt.Color(73, 128, 242));
        page.setLayout(new java.awt.BorderLayout());
        getContentPane().add(page, java.awt.BorderLayout.CENTER);

        setSize(new java.awt.Dimension(800, 500));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents
    
  
    

    
    public void changecolor(JPanel hover, Color rand){
        hover.setBackground(rand);
    }
    
    public void clickmenu(JPanel h1, JPanel h2, int numberbool){
        if(numberbool == 1){
            h1.setBackground(new Color(245, 245, 245));

        }
        else{
            h1.setBackground(new Color(245, 245, 245));
            h2.setBackground(new Color(25, 29, 74));
        }
    }
    
    public void changeimage(JLabel button, String resourcheimg){
        ImageIcon aimg = new ImageIcon(getClass().getResource(resourcheimg));
        button.setIcon(aimg);
    }
    
    public void hideshow(JPanel menushowhide, boolean dashboard, JLabel button){
        if(dashboard == true){
            menushowhide.setPreferredSize(new Dimension(50, menushowhide.getHeight()));
            changeimage(button, "/Icon/menu_32px.png");
        }
        else{
            menushowhide.setPreferredSize(new Dimension(270, menushowhide.getHeight()));
            changeimage(button, "/Icon/back_32px.png");
        }
        
    }
    
    private void closeMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeMouseEntered
        changecolor(Buttonclose, new Color(245, 245, 245));
    }//GEN-LAST:event_closeMouseEntered

    private void closeMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeMouseExited
     changecolor(Buttonclose, new Color(250, 250, 250));
    }//GEN-LAST:event_closeMouseExited

    private void closeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeMouseClicked
        System.exit(0);
    }//GEN-LAST:event_closeMouseClicked

    private void fullmaxMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fullmaxMouseEntered
        changecolor(Buttonmax, new Color(245, 245, 245));
    }//GEN-LAST:event_fullmaxMouseEntered

    private void fullmaxMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fullmaxMouseExited
        changecolor(Buttonmax, new Color(250, 250, 250));
    }//GEN-LAST:event_fullmaxMouseExited

    private void fullmaxMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fullmaxMouseClicked
        if(this.getExtendedState()!= Mainmenu.MAXIMIZED_BOTH){
          this.setExtendedState(Mainmenu.MAXIMIZED_BOTH);
        }
        else{
            this.setExtendedState(Mainmenu.NORMAL);
        }
    }//GEN-LAST:event_fullmaxMouseClicked

    private void buttonhidemenuMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonhidemenuMouseEntered
        changecolor(hidemenu, new Color(245, 245, 245));
    }//GEN-LAST:event_buttonhidemenuMouseEntered

    private void buttonhidemenuMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonhidemenuMouseExited
        changecolor(hidemenu, new Color(250, 250, 250));
    }//GEN-LAST:event_buttonhidemenuMouseExited

    private void buttonhidemenuMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonhidemenuMouseClicked
        clickmenu(hidemenu, setting, 1);
        //create void for methode hide and show panel menu
        if(a==true){
          hideshow(menu, a, buttonhidemenu);
          SwingUtilities.updateComponentTreeUI(this);
          //create methode change image
          
          a=false;
        }
        else{
            hideshow(menu, a, buttonhidemenu);
            SwingUtilities.updateComponentTreeUI(this);
            a=true;
        }
        
    }//GEN-LAST:event_buttonhidemenuMouseClicked

    private void ButtonsettingMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ButtonsettingMouseEntered
        changecolor(setting, new Color(245, 245, 245));
    }//GEN-LAST:event_ButtonsettingMouseEntered

    private void ButtonsettingMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ButtonsettingMouseExited
        changecolor(setting, new Color(250, 250, 250));
    }//GEN-LAST:event_ButtonsettingMouseExited

    private void ButtonsettingMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ButtonsettingMouseClicked
        clickmenu(setting, hidemenu, 1);
    }//GEN-LAST:event_ButtonsettingMouseClicked

    private void dashboardbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dashboardbtnActionPerformed
    setActiveButton(dashboardbtn);
    page.removeAll();
    page.add(dashboardPanel);
    page.revalidate();
    page.repaint();
    }//GEN-LAST:event_dashboardbtnActionPerformed

    private void databarangbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_databarangbtnActionPerformed
    setActiveButton(databarangbtn);
    showDataBarangPanel();
    }//GEN-LAST:event_databarangbtnActionPerformed

    private void settingMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_settingMouseEntered
               changecolor(setting, new Color(245, 245, 245)); // TODO add your handling code here:
    }//GEN-LAST:event_settingMouseEntered

    private void datapenggunabtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_datapenggunabtnActionPerformed
    setActiveButton(datapenggunabtn);
    showDataPengguna();   // TODO add your handling code here:        // TODO add your handling code here:
    }//GEN-LAST:event_datapenggunabtnActionPerformed

    private void datagurubtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_datagurubtnActionPerformed
    setActiveButton(datagurubtn);
        showDataGuru();   // TODO add your handling code here:
    }//GEN-LAST:event_datagurubtnActionPerformed

    private void datasupplierbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_datasupplierbtnActionPerformed
    setActiveButton(datasupplierbtn);
        showDataSupplier();  // TODO add your handling code here:
    }//GEN-LAST:event_datasupplierbtnActionPerformed

    private void transaksipenjualanbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transaksipenjualanbtnActionPerformed
    setActiveButton(transaksipenjualanbtn);
        page.removeAll();
    page.add(transaksipenjualanpanel);
    page.revalidate();
    page.repaint();     // TODO add your handling code here:
    }//GEN-LAST:event_transaksipenjualanbtnActionPerformed

    private void datahutangbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_datahutangbtnActionPerformed
    setActiveButton(datahutangbtn);
        showDataHutang();      // TODO add your handling code here:
    }//GEN-LAST:event_datahutangbtnActionPerformed

    private void keluarbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keluarbtnActionPerformed
    setActiveButton(keluarbtn);    // TODO add your handling code here:
    }//GEN-LAST:event_keluarbtnActionPerformed

    private void transaksipembelianbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transaksipembelianbtnActionPerformed
    setActiveButton(transaksipembelianbtn);
    page.removeAll();
    page.add(transaksipembelianpanel);
    page.revalidate();
    page.repaint();        // TODO add your handling code here:
    }//GEN-LAST:event_transaksipembelianbtnActionPerformed

    private void laporanpenjualanbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_laporanpenjualanbtnActionPerformed
    setActiveButton(laporanpenjualanbtn);
    page.removeAll();
    page.add(datalaporanpenjualanpanel);
    page.revalidate();
    page.repaint();        // TODO add your handling code here:
    }//GEN-LAST:event_laporanpenjualanbtnActionPerformed

    private void laporanpembelianbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_laporanpembelianbtnActionPerformed
    setActiveButton(laporanpembelianbtn);
    page.removeAll();
    page.add(datalaporanpembelianpanel);
    page.revalidate();
    page.repaint();         // TODO add your handling code here:
    }//GEN-LAST:event_laporanpembelianbtnActionPerformed

    private void voucherbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_voucherbtnActionPerformed
    setActiveButton(voucherbtn);
        showDataVoucher();        // TODO add your handling code here:
    }//GEN-LAST:event_voucherbtnActionPerformed

    private void datakategoribtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_datakategoribtnActionPerformed
    setActiveButton(datakategoribtn);
        showdatakategori();  // TODO add your handling code here:
    }//GEN-LAST:event_datakategoribtnActionPerformed


    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
try {
UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
} catch (Exception e) {
    e.printStackTrace();
}

        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Mainmenu().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Buttonclose;
    private javax.swing.JPanel Buttonmax;
    private javax.swing.JLabel Buttonsetting;
    private javax.swing.JPanel Header;
    private javax.swing.JPanel MenuIcon;
    private javax.swing.JLabel buttonhidemenu;
    private javax.swing.JLabel close;
    private javax.swing.JButton dashboardbtn;
    private javax.swing.JButton databarangbtn;
    private javax.swing.JButton datagurubtn;
    private javax.swing.JButton datahutangbtn;
    private javax.swing.JButton datakategoribtn;
    private javax.swing.JButton datapenggunabtn;
    private javax.swing.JButton datasupplierbtn;
    private javax.swing.JLabel fullmax;
    private javax.swing.JPanel hidemenu;
    private javax.swing.JPanel iconminmaxclose;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JButton keluarbtn;
    private javax.swing.JButton laporanpembelianbtn;
    private javax.swing.JButton laporanpenjualanbtn;
    private javax.swing.JPanel linehidemenu;
    private javax.swing.JPanel lineseting;
    private javax.swing.JPanel menu;
    private javax.swing.JPanel menuhide;
    private javax.swing.JPanel page;
    private javax.swing.JPanel setting;
    private javax.swing.JButton transaksipembelianbtn;
    private javax.swing.JButton transaksipenjualanbtn;
    private javax.swing.JButton voucherbtn;
    // End of variables declaration//GEN-END:variables

}

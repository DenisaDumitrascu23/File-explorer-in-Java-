import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Stack;
import java.util.zip.*;

public class FileExplorer extends JFrame {

    private JTextField campCale = new JTextField();
    private JButton butonDeschide = new JButton("Deschide");
    private JButton butonInapoi = new JButton("Înapoi");
    private JButton butonArhiveaza = new JButton("Arhivează");
    private JButton butonDezarhiveaza = new JButton("Dezarhivează");
    private JList<String> listaFisiere = new JList<>();
    private DefaultListModel<String> modelLista = new DefaultListModel<>();

    private File directorCurent;
    private Stack<File> istoric = new Stack<>();

    public FileExplorer() {
        setTitle("File Explorer");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));

        JPanel panouSus = new JPanel(new BorderLayout(5, 5));
        panouSus.add(campCale, BorderLayout.CENTER);

        JPanel panouButoane = new JPanel();
        panouButoane.add(butonDeschide);
        panouButoane.add(butonInapoi);
        panouButoane.add(butonArhiveaza);
        panouButoane.add(butonDezarhiveaza);

        panouSus.add(panouButoane, BorderLayout.EAST);
        add(panouSus, BorderLayout.NORTH);

        listaFisiere.setModel(modelLista);
        add(new JScrollPane(listaFisiere), BorderLayout.CENTER);

        butonDeschide.addActionListener(e -> deschideSelectieSauCale());
        butonInapoi.addActionListener(e -> mergiInapoi());
        butonArhiveaza.addActionListener(e -> arhiveazaFisier());
        butonDezarhiveaza.addActionListener(e -> dezarhiveazaFisier());

        // inceput, arata partitiile
        directorCurent = null;
        campCale.setText("My Computer");
        afiseazaPartitii();
        // ---------------------------------------------------------

        setVisible(true);
    }
//afiseaza partitii cu drive
    private void afiseazaPartitii() {
        modelLista.clear();
        campCale.setText("My Computer");

        File[] partitii = File.listRoots();
        for (File p : partitii) {
            modelLista.addElement("[DRIVE] " + p.getAbsolutePath());
        }
    }

    private void deschideSelectieSauCale() {
        String selectat = listaFisiere.getSelectedValue();

        if (selectat != null) {
            deschideElementSelectat(selectat);
        } else {
            deschideCaleaScrisa();
        }
    }

    private void deschideCaleaScrisa() {
        File director = new File(campCale.getText());

        if (director.exists() && director.isDirectory()) {
            istoric.push(directorCurent);
            directorCurent = director;
            afiseazaFisiere(director);
        } else {
            JOptionPane.showMessageDialog(this, "Calea nu este validă!");
        }
    }

    private void mergiInapoi() {
        if (istoric.isEmpty()) {
            directorCurent = null;
            afiseazaPartitii();
            return;
        }

        directorCurent = istoric.pop();
        campCale.setText(directorCurent.getAbsolutePath());
        afiseazaFisiere(directorCurent);
    }

    private void deschideElementSelectat(String selectat) {

        // verifica daca e drive
        if (selectat.startsWith("[DRIVE] ")) {
            String cale = selectat.replace("[DRIVE] ", "");
            File drive = new File(cale);

            if (drive.exists() && drive.isDirectory()) {
                istoric.push(directorCurent);
                directorCurent = drive;
                afiseazaFisiere(drive);
            }
            return;
        }

        // dir, navigare prin file dintrun drive
        String nume = selectat.replace("[DIR] ", "");
        File fisierSelectat = new File(directorCurent, nume);

        if (fisierSelectat.isDirectory()) {
            istoric.push(directorCurent);
            directorCurent = fisierSelectat;
            afiseazaFisiere(fisierSelectat);
        } else {
            JOptionPane.showMessageDialog(this, "Ai selectat:\n" + fisierSelectat.getAbsolutePath());
        }
    }

    private void afiseazaFisiere(File director) {
        modelLista.clear();
        campCale.setText(director.getAbsolutePath());

        File[] fisiere = director.listFiles();
        if (fisiere == null) {
            modelLista.addElement("Nu poti citi folderul!");
            return;
        }

        for (File element : fisiere) {
            if (element.isDirectory()) {
                modelLista.addElement("[DIR] " + element.getName());
            } else {
                modelLista.addElement(element.getName());
            }
        }
    }

    private void arhiveazaFisier() {
        String selectat = listaFisiere.getSelectedValue();

        if (selectat == null) {
            JOptionPane.showMessageDialog(this, "Selectează un fișier!");
            return;
        }

        File fisier = new File(directorCurent, selectat.replace("[DIR] ", ""));

        if (!fisier.exists() || fisier.isDirectory()) {
            JOptionPane.showMessageDialog(this, "Nu pot arhiva foldere, doar fișiere.");
            return;
        }

        File zipFile = new File(fisier.getAbsolutePath() + ".zip");

        try {
            FileInputStream fis = new FileInputStream(fisier);
            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));

            ZipEntry entry = new ZipEntry(fisier.getName());
            zipOut.putNextEntry(entry);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                zipOut.write(buffer, 0, len);
            }

            zipOut.closeEntry();
            zipOut.close();
            fis.close();

            JOptionPane.showMessageDialog(this, "Fișier arhivat!");
            afiseazaFisiere(directorCurent);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Eroare la arhivare!");
        }
    }

    private void dezarhiveazaFisier() {
        String selectat = listaFisiere.getSelectedValue();

        if (selectat == null || !selectat.endsWith(".zip")) {
            JOptionPane.showMessageDialog(this, "Selectează un fișier arhivat");
            return;
        }

        File zipFile = new File(directorCurent, selectat);

        try {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry entry = zis.getNextEntry();

            if (entry == null) {
                JOptionPane.showMessageDialog(this, "Arhiva este goală!");
                return;
            }

            File outFile = new File(directorCurent, entry.getName());
            FileOutputStream fos = new FileOutputStream(outFile);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }

            fos.close();
            zis.closeEntry();
            zis.close();

            JOptionPane.showMessageDialog(this, "Fișier dezarhivat!");
            afiseazaFisiere(directorCurent);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Eroare la dezarhivare!");
        }
    }

    public static void main(String[] args) {
        new FileExplorer();
    }
}

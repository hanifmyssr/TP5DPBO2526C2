# TP5 DPBO - PeopleData (Data Anggota Polri) dengan Database MySQL

**Nama:** Muhammad Hanif Muyassar
**NIM:** (isi NIM)
**Kelas:** (isi kelas)

## Janji
Saya mengerjakan Tugas Praktikum 5 DPBO dengan sebaik-baiknya dan tanpa melakukan kecurangan. Aamiin.

## Desain & Fungsi Program
Aplikasi desktop Java Swing untuk mengelola **data anggota Polri** (data fiktif). Seluruh operasi CRUD sekarang terhubung ke **database MySQL** (`db_people`) lewat JDBC, dan penggunaan `ArrayList` sudah dihapus.

**Struktur project**
- `Database.java`: membuka koneksi ke MySQL (`jdbc:mysql://localhost:3306/db_people`) dan menyediakan objek `Connection`.
- `PeopleMenu.java`: class utama (extends `JFrame`), berisi logika CRUD dan listener. Semua query memakai `PreparedStatement`.
- `PeopleMenu.form`: desain GUI (JTextField, JComboBox, JButton, JTable di dalam JScrollPane).
- `db_people.sql`: skema dan data awal database.

**Tabel `person`**

| Kolom | Tipe | Keterangan |
|---|---|---|
| id | VARCHAR(20), primary key | ID anggota |
| nama | VARCHAR(100) | nama anggota |
| tahun_lahir | INT | tahun lahir |
| kategori | VARCHAR(50) | satuan Polri (JComboBox) |
| pangkat | VARCHAR(30) | atribut baru (JComboBox) |

**Method di PeopleMenu**
- `setTable()`: menjalankan `SELECT` lalu mengisi tabel dari database.
- `insertData()`: validasi input, cek ID kembar, lalu `INSERT`.
- `updateData()`: validasi input, cek ID kembar (jika ID diubah), lalu `UPDATE`.
- `deleteData()`: dialog konfirmasi Yes/No, lalu `DELETE`.
- `clearForm()`: mengosongkan form dan kembali ke mode Add.
- `idExists()`: mengecek apakah ID sudah ada di database.
- `readForm()`: membaca form dan menampilkan dialog error jika ada kolom kosong atau input tidak valid.

**Dialog error**
- Kolom input masih kosong saat Add atau Update.
- ID sudah dipakai saat Add (atau saat Update mengganti ID menjadi ID yang sudah ada).
- Tahun lahir bukan angka, atau usia kurang dari 18 tahun.
- Koneksi database gagal.

**Tanpa ArrayList:** saat baris tabel diklik, nilai ID, nama, tahun lahir, satuan, dan pangkat diambil langsung dari `TableModel`. ID baris yang dipilih disimpan di variabel `selectedId` untuk dipakai pada Update dan Delete.

## Cara Menjalankan
1. Jalankan MySQL (XAMPP atau Laragon), lalu import `db_people.sql`.
2. Download MySQL Connector/J, taruh file `.jar` di folder `lib`, lalu tambahkan sebagai library lewat Project Structure, Modules, Dependencies.
3. Sesuaikan `URL`, `USER`, dan `PASSWORD` di `Database.java` bila perlu.
4. Jalankan `main()` di `PeopleMenu`.

## Alur Program
1. Program terhubung ke database, lalu menampilkan seluruh data di tabel.
2. Mode **Add**: isi form lalu klik **Add**. Data masuk ke database dan tabel dimuat ulang.
3. Klik salah satu baris tabel: form terisi, tombol berubah menjadi **Update** dan tombol **Delete** muncul.
4. Klik **Update** untuk menyimpan perubahan ke database.
5. Klik **Delete**, muncul konfirmasi; pilih Yes untuk menghapus dari database.
6. Klik **Cancel** untuk mengosongkan form dan kembali ke mode Add.

## Dokumentasi
(Tambahkan screenshot atau rekaman yang menunjukkan semua operasi CRUD dan semua dialog error.)

![Tampilan awal](Dokumentasi/awal.png)
![Create](Dokumentasi/create.png)
![Update](Dokumentasi/update.png)
![Konfirmasi Delete](Dokumentasi/konfirmasi-delete.png)
![Error kolom kosong](Dokumentasi/error-kosong.png)
![Error ID kembar](Dokumentasi/error-id-kembar.png)

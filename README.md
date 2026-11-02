# Kost Goldyz (Android)

Aplikasi Android untuk membuka Kost Goldyz & Kontrakan di HP, dengan ikon sendiri di layar utama.

Semua fitur sama dengan versi di Claude, karena datanya dimuat dari sana:
- menu beranda, kostan, kontrakan, dan maintenance
- unggah bukti transfer yang dibaca AI, dan pembayaran tunai
- tombol WhatsApp dan Google Calendar, yang dibuka di aplikasinya masing-masing

Butuh internet, dan saat pertama dibuka harus **masuk ke akun Claude** yang sama.

## Build APK lewat GitHub
1. Buat repository baru di GitHub, lalu upload semua isi folder ini.
2. Kalau folder `.github` tidak ikut ter-upload, buka tab **Actions**, pilih **set up a workflow yourself**, beri nama `build-apk.yml`, lalu paste isi file `.github/workflows/build-apk.yml`.
3. Tunggu sampai muncul centang hijau, lalu download **KostGoldyz-apk** dari bagian **Artifacts**.

## Mengganti alamat aplikasi
Alamat halaman ada di `app/src/main/res/values/strings.xml` (`app_url`).

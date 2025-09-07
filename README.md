# GezGezAglr — Gezgin (Android / Jetpack Compose)

## Özellikler
- **Alt sekmeli gezinti:** Ana Sayfa, Ara, Ekle, Profil.
- **Gönderi paylasimi:** Baslik, aciklama, 0–10 puan, fotograf, konum bilgisi.
- **Konum secimi:** Mevcut konumu alma + harita üzerinden nokta secme.
- **Akis:** Takip edilen kullanicilarin gönderileri yeni→eski sirali listelenir.
- **Profil:** Profil fotografi, bio, takipçi/takip edilen sayilari, kullanici gönderileri.
- **Arama:** Kullanici adina göre arama (Firestore baslangic/bitis araligi).

## Teknolojiler
- **Kotlin**, **Jetpack Compose (Material3)**
- **Firebase Auth**, **Cloud Firestore**, **Firebase Storage**
- **Coil** (görseller), **OSMDroid** (harita), **Google Location Services**

## Ekranlar
- **HomeScreen:** Ana akis ve alt gezinme.
- **SearchTabScreen:** Kullanici arama.
- **CreatePostScreen:** Fotograf ekleme, puanlama, konum secimi ve paylasim.
- **UserProfileScreen / EditProfileScreen:** Profil görüntüleme ve düzenleme.
- **Followers/Following:** (Istege bagli/planlanan rotalar).

## Kurulum
1. Depoyu klonlayin:
   ```bash
   git clone https://github.com/mertcantopcu01/GezGezAglr.git
   ```
2. Android Studio (Giraffe+ / Iguana+) ile acin.
3. **Firebase kurulumu:**
   - Firebase Console’da bir proje olusturun.
   - `google-services.json` dosyasini `app/` klasörüne ekleyin.
   - Authentication (Email/Password), Firestore ve Storage’i etkinlestirin.
4. OSMDroid ve konum izinleri icin `AndroidManifest.xml`’de gerekli izinleri ekleyin:
   ```xml
   <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
   <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
   ```
5. Calistirin.

## Proje Yapisi (kisa)
```
app/src/main/java/com/example/myapplication/
 ├─ firebase/        # AuthService, FirestoreService, FirebaseStorageService
 ├─ screens/         # Home, Search, CreatePost, Profile vs.
 ├─ navigation/      # (Varsa) route & NavHost düzeni
 └─ ui/              # Tema ve yardimci UI bilesenleri
```

## Önemli Notlar
- **Puanlama:** UI’da 0–5 yildiz, Firestore’a 0–10 olarak yaziliyor.
- **Görsel yükleme:** Storage’a yüklendikten sonra indirme URL’si gönderiye kaydedilir.
- **Konum:** Izinler verilmezse harita secimiyle konum ekleme yoluna gidilebilir.

## Yol Haritasi / TODO
- [ ] Gönderi detay ekrani (yorumlar, begeniler)
- [ ] Takip akisinda sonsuz kaydirma/paging
- [ ] Profil düzenlemede ek alanlar (kullanici adi benzersizligi vb.)
- [ ] Hata ve bos durum ekranlarinin iyilestirilmesi
- [ ] Unit/UI testleri

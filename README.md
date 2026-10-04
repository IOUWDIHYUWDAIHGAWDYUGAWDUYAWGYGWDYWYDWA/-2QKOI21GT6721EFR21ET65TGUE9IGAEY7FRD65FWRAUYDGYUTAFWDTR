# Real Life Earth — NeoForge 1.21.1

Gerçek dünya haritasından doğma konumu seç, OSM verisiyle şehirler kur, MCA tarzı gerçekçi NPC'lerle yaşa.

## Özellikler
- **Dünya oluşturma menüsü override**: Tek oyuncu > Yeni Dünya -> Gerçek dünya haritası (OSM raster tiles), zoom/pan (tekerlek/sürükle), haritada tıklayarak pin, sağ panelden hızlı şehir seçimi (İstanbul/Ankara/Tokyo/NYC/Berlin/Paris) ve **Ara** (Nominatim) ile her yeri bul.
- **Spawn kaydı**: Seçilen lat/lon `RealEarthData` ile ilk girişte oyuncuya uygulanır, `/reallife spawninfo` ile görüntülenir.
- **Şehir inşa komutları**:
  - `/reallife buildcity <lat> <lon> <radiusKm>` — belirtilen koordinat etrafında Overpass'ten `building` + `highway` çeker, binaları (kat yüksekliği OSM `building:levels`/`height` veya ülkeye göre) ve yolları (genişlik highway tipine göre) dünyaya yerleştirir.
  - `/reallife buildhere <radiusKm>` — seçili spawn veya oyuncu konumu etrafında inşa eder.
  - `/reallife search <query>` — Nominatim arama, ilk 5 sonucu komut önerisiyle yazar.
  - `/reallife spawnnpc <count>` — yakına 1-50 NPC spawn eder.
- **NPC**: `RealNpcEntity` — MCA tarzı blocky, cinsiyet/ülke/yaş/meslek/kişilik + boy skalası (ülke ortalamasına göre). 16 meslek, ev/iş konumu, sağ tık diyalog.
- **Sistemler**: Para (`/reallife` ekonomisi attachment), Susuzluk/Enerji/Hijyen (config ile kapatılabilir), Telefon GUI.
- **Bloklar/Itemlar**: Asfalt, kaldırım, raf, masa + telefon/banka kartı/cüzdan/anahtar.
- **Diller**: `en_us` + `tr_tr`.

## Kurulum (Geliştirici)
```
./gradlew build          # 1.21.1 için derle -> build/libs/reallifeearth-1.0.0.jar
./gradlew runClient      # iç IDE test
./gradlew runServer      # server test
```
Java 21 gerektirir.

## Kullanım (Oyuncu)
1. Modu `mods/` klasörüne at.
2. Oyunu aç > Tek Oyunculu > **Yeni Dünya Oluştur** -> Harita ekranı gelir.
3. Haritayı sürükle/zoomla veya sağ panelden şehir seç / arama yap, haritada noktaya tıkla (kırmızı pin), **Dünyayı Oluştur** de.
4. Vanilla dünya oluşturma ekranı açılır, normal şekilde dünyayı oluştur. İlk girişte spawn ayarlanır.
5. Dünyada `/reallife buildhere 1` yaz (1km yarıçap şehir kurar, internet gerektirir). Ardından `/reallife spawnnpc 10` ile nüfus ekle.

## Teknik Notlar
- OSM Tiles: `tile.openstreetmap.org/{z}/{x}/{y}.png` (User-Agent: RealLifeEarth/1.0)
- Overpass: `overpass-api.de` + fallback, bbox `way["building"]` + `way["highway"]`
- Nominatim: `nominatim.openstreetmap.org/search`
- Cache: `world/data/reallifeearth/osm_cache/` (gelecek: disk cache)
- Koordinat dönüşümü: 1 blok ≈ 2m, origin = seçilen lat/lon
- NeoForge MDK: 1.21 / 21.1.176, Parchment 2024.11.17, Gradle 8.14.2

## Yol Haritası
- Fase 2 ChunkGenerator entegrasyonu (şehir otomatik chunk'ta)
- Kiralık ev / banka faizi / polis
- Araba entity
- Daha fazla NPC AI (işe git/eve dön, sosyal)

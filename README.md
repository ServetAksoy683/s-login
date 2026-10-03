# S-Login (v1.0.0)

Paper/Folia sunucular icin basit, sifre korumali giris (login) plugini.
Giris yapmayan oyuncular hicbir sey goremez (korluk efekti), yerinde donar ve
ustlerinde geri sayan bir boss bar gorur. Sure dolarsa ya da sifreyi 4 kez
yanlis girerlerse sunucudan atilirlar.

## Komutlar

| Komut | Aciklama |
|---|---|
| `/sregister <sifre> <sifre-tekrar>` | Ilk kez giren oyuncu hesap olusturur |
| `/slogin <sifre>` | Mevcut hesaba giris yapar |
| `/spasswordreset <oyuncu>` | (Sadece yetkili) Oyuncunun sifresini sifirlar |
| `/schange <yeni-sifre> <yeni-sifre-tekrar>` | Sifre degistirme istegi olusturur |
| `/sconfirm` | Bekleyen sifre degisikligini onaylar |

## Nasil calisir

1. Oyuncu sunucuya girer: ekrani kararir (korluk + karanlik efekti —
   gercek "End atmosferi" bir API olmadigi icin diger login pluginlerinin
   kullandigi bu yontemle, ekran tamamen karartilarak taklit edildi),
   yerinde donar ve ustte 120 saniyelik bir boss bar sayaci baslar.
   Sayac her saniye azalirken "tik" sesi calar, son 10 saniyede ses daha
   sik/yuksek tona gecer.
2. **Hesabi yoksa**: `/sregister <sifre> <sifre-tekrar>` yazar, basarili
   olursa aninda kilidi acilir (goruyor, hareket edebiliyor).
3. **Hesabi varsa**: `/slogin <sifre>` yazar. Yanlis girerse kalan deneme
   hakki soylenir; 4. yanlis denemede aninda sunucudan atilir.
4. **120 saniye icinde hicbir sey yapmazsa**: otomatik atilir.
5. Giris yapana kadar sohbet edemez ve `/sregister`, `/slogin`, `/schange`,
   `/sconfirm` disinda komut kullanamaz.

### Sifre sifirlama akisi

1. Yetkili: `/spasswordreset <oyuncu>` yazar (oyuncu online olmali).
2. O oyuncu tekrar kilitlenir (ya da zaten kilitliyse kilitli kalir) ve
   `/schange <yeni-sifre> <yeni-sifre-tekrar>` yazmasi istenir.
3. `/schange` sonrasi `/sconfirm` ile onaylamasi gerekir.
4. Onayladiktan sonra **yeni sifresiyle tekrar `/slogin` yapmak zorundadir**
   — hesabina otomatik giris yapilmaz.

Zaten giris yapmis bir oyuncu da istedigi zaman kendi sifresini
`/schange` + `/sconfirm` ile degistirebilir; bu durumda tekrar giris
yapmasina gerek kalmaz, oturumu acik kalir.

## Ayarlar (config.yml)

- `language`: `turkish`, `russia`, `english`
- `login-timeout-seconds`: giris icin tanilan sure (varsayilan 120)
- `max-failed-attempts`: izin verilen yanlis sifre denemesi (varsayilan 4)
- `min-password-length`: minimum sifre uzunlugu (varsayilan 4)
- `blind-effect`, `freeze-movement`, `prevent-damage`, `block-chat-and-commands`:
  hepsi ayri ayri acilip kapatilabilir
- `sounds`: her olay icin ayri ses, ses ismini bos birakarak kapatilabilir

## Guvenlik notu

Sifreler asla duz metin olarak saklanmaz. Her hesap icin rastgele bir "tuz"
(salt) uretilir ve sifre + tuz birlikte SHA-256 ile hashlenip
`plugins/S-Login/accounts.yml` dosyasina öyle yazilir.

## Derleme

```
cd s-login
mvn clean package
```

JAR: `target/S-Login-1.0.0.jar`. GitHub Actions ile otomatik derleme icin
`.github/workflows/build.yml` zaten hazir, repo'ya push ettiginde Actions
sekmesinden JAR'i indirebilirsin.

## Desteklenen altyapilar

Paper, Purpur, Folia, Canvas — Minecraft 1.19 ve ustu.

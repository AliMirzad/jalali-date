# 📅 Jalali Date Library (jalali-date)

کتابخانه‌ای سبک و دقیق برای کار با تاریخ جلالی (شمسی) در جاوا — با پشتیبانی از تبدیل تقویم، محاسبات ماهانه، و ادغام کامل با `java.time`.

---

## 🚀 ویژگی‌ها

- پیاده‌سازی کامل تاریخ شمسی (`PersianDate`)
- تبدیل دوطرفه بین **Jalali ↔ Gregorian**
- پشتیبانی از `TemporalAdjuster` برای تاریخ‌های خاص مثل:
  - nاُمین روز هفته در ماه (`nthInMonth`)
  - آخرین روز هفته در ماه (`lastInMonth`)
- سازگار با `LocalDate` و `ZoneId.of("Asia/Tehran")`
- بدون وابستگی خارجی (Pure Java)
- تست‌شده با **JUnit5** و گزارش پوشش **JaCoCo**

---

## ⚙️ نصب

اگر از **Maven** استفاده می‌کنی:

```xml
<dependency>
  <groupId>ir.amirzad.jalali</groupId>
  <artifactId>jalali-date</artifactId>
  <version>1.0.0</version>
</dependency>
```

یا در **Gradle**:

```gradle
implementation 'ir.amirzad.jalali:jalali-date:1.0.0'
```

---

## 💡 استفاده سریع

```java
import ir.amirzad.jalali.PersianDate;
import ir.amirzad.jalali.PersianAdjusters;
import java.time.DayOfWeek;

public class Example {
    public static void main(String[] args) {
        PersianDate base = PersianDate.of(1403, 1, 1);

        // تبدیل به میلادی
        System.out.println(base.toGregorian()); // 2024-03-20

        // پیدا کردن سومین چهارشنبه ماه
        PersianDate thirdWed = PersianAdjusters.nthInMonth(base, DayOfWeek.WEDNESDAY, 3);
        System.out.println(thirdWed); // 1403-01-15

        // آخرین جمعه ماه
        PersianDate lastFri = PersianAdjusters.lastInMonth(base, DayOfWeek.FRIDAY);
        System.out.println(lastFri); // 1403-01-31
    }
}
```

---

## 🧪 تست و کاوریج

اجرای تمام تست‌ها:

```bash
mvn test
```

تولید گزارش پوشش (JaCoCo):

```bash
mvn jacoco:report
```

📊 گزارش HTML در مسیر زیر ساخته می‌شود:

```
target/site/jacoco/index.html
```

---

## 🧩 ساختار پروژه

```
jalali-date/
 ├── pom.xml
 ├── README.md
 ├── src/
 │   ├── main/java/ir/amirzad/jalali/
 │   │   ├── PersianDate.java
 │   │   ├── PersianAdjusters.java
 │   │   └── DateInterop.java
 │   └── test/java/ir/amirzad/jalali/
 │       ├── PersianDateTest.java
 │       └── AdjusterTest.java
 └── target/
```

---

## 🧠 طراحی

> الهام‌گرفته از API استاندارد `java.time`  
> تمام کلاس‌ها **Immutable** و **Thread-safe** هستند.  
> نام‌گذاری‌ها و متدها مشابه کلاس‌های جاوایی مثل `LocalDate` طراحی شده‌اند تا یادگیری سریع باشد.

---

## 🪄 توابع کلیدی

| کلاس | توضیح | نمونه استفاده |
|------|--------|----------------|
| `PersianDate` | ساخت و نگهداری تاریخ شمسی | `PersianDate.of(1403,7,15)` |
| `PersianAdjusters` | ابزار محاسبه تاریخ‌های خاص | `nthInMonth(date, DayOfWeek.WEDNESDAY, 3)` |
| `DateInterop` | تبدیل بین میلادی و شمسی | `fromJalali(1403,1,1)` |

---

## 👨‍💻 توسعه‌دهنده
**Amirzad**  
📧 [alimirzad99@gmail.com](mailto:alimirzad99@gmail.com)  
🌐 [github.com/AliMirzad](https://github.com/AliMirzad)

---

## ⭐ پشتیبانی

اگر این پروژه برات مفید بود، لطفاً با ⭐ دادن در GitHub ازش حمایت کن!

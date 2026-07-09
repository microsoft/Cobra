package com.nuance.ncs.nmsp.lang;

import java.util.HashMap;
import java.util.Map;

public class NCSQueryRetryAndErrorMessages
{
    private static Map<String, String> noMatchPrompts = buildNoMatchPrompts();
    private static Map<String, String> noInputPrompts = buildNoInputPrompts();

    private static final String DEFAULT = "default";

    NCSQueryRetryAndErrorMessages() { throw new IllegalStateException("Utility class!"); }

    @SuppressWarnings("java:S1192")
    private static Map<String, String> buildNoMatchPrompts()
    {
        Map<String, String> rc = new HashMap<>();
        rc.put(DEFAULT, "Sorry, speech not recognized. Please try again.");
        rc.put("fra-FRA", "Dictée non reconnue. Veuillez réessayer.");
        rc.put("fra-CAN", "Dictée non reconnue. Veuillez réessayer.");
        rc.put("por-BRA", "A fala não foi reconhecida. Tente novamente.");
        rc.put("por-PRT", "A fala não foi reconhecida. Tente de novo.");
        rc.put("ita-ITA", "Dettato non riconosciuto. Riprova.");
        rc.put("deu-DEU", "Die Aufnahme wurde leider nicht erkannt. Bitte versuchen Sie es erneut.");
        rc.put("spa-ESP", "Lo sentimos. No se reconoce la voz. Intente de nuevo.");
        rc.put("spa-XLA", "Lo sentimos. No se reconoce la voz. Intente de nuevo.");
        rc.put("spa-USA", "Lo sentimos. No se reconoce la voz. Intente de nuevo.");
        rc.put("cmn-CHN", "抱歉，无法识别，请重试。");
        rc.put("cmn-TWN", "抱歉，無法識別，請重試。");
        rc.put("yue-CHN", "抱歉，無法識別，請重試。");
        rc.put("heb-ISR", "מצטערים, הדיבור לא מזוהה. בבקשה נסה שוב.");
        rc.put("jpn-JPN", "申し訳ありません、発話が聞き取れませんでした。もう一度試して下さい。");
        rc.put("swe-SWE", "Tyvärr uppfattades inte vad du sade. Vänligen försök igen.");
        rc.put("rus-RUS", "Речь не распознана. Пожалуйста, попробуйте ещё раз.");
        rc.put("pol-POL", "Rozpoznawanie mowy nie powiodlo sie. Spróbuj ponownie.");
        rc.put("hun-HUN", "Nem sikerült a beszédfelismerés. Próbálja újra.");
        rc.put("fin-FIN", "Puhetta ei tunnistettu. Yritä uudelleen.");
        rc.put("ell-GRC", "Λυπούμαστε, η ομιλία δεν αναγνωρίζεται. ΠΑΡΑΚΑΛΩ προσπαθησε ξανα.");
        rc.put("ces-CZE", "Omlouváme se, řeč nebyla rozpoznána. Prosím zkuste to znovu.");
        rc.put("ron-ROU", "Ne pare rău, vorbirea nu a fost recunoscută. Vă rugăm să încercați din nou.");
        rc.put("nor-NOR", "Beklager, tale ikke gjenkjent. Prøv igjen.");
        rc.put("nld-NLD", "De spraak wordt niet herkend. Probeer het opnieuw.");
        rc.put("zlm-MYS", "Maaf, ucapan tidak dikenali. Sila cuba lagi.");
        rc.put("slk-SVK", "Ľutujeme, reč nebola rozpoznaná. Prosím skúste znova.");
        rc.put("ukr-UKR", "Вибачте, мовлення не розпізнано. Будь ласка спробуйте ще раз.");
        rc.put("hrv-HRV", "Nažalost, govor nije prepoznat. Molim te pokušaj ponovno.");
        rc.put("kor-KOR", "죄송합니다. 인식하지 못했습니다. 다시 시도해 주십시오.");
        rc.put("dan-DNK", "Beklager, talen blev ikke genkendt. Prøv igen.");
        rc.put("ara-XWW", "عذرا ، الكلام غير معروف. حاول مرة اخرى.");
        rc.put("tur-TUR", "Maalesef konuşma tanınmadı. Lütfen tekrar deneyin.");
        rc.put("vie-VNM", "Xin lỗi, giọng nói không được công nhận. Vui lòng thử lại.");
        rc.put("ind-IDN", "Maaf, ucapan tidak dikenali. Silakan coba lagi.");
        rc.put("tha-THA", "ขออภัย ไม่รู้จักคำพูด กรุณาลองอีกครั้ง.");
        rc.put("cat-ESP", "Ho sentim, no es reconeix la veu. Siusplau torna-ho a provar.");
        return rc;
    }

    @SuppressWarnings("java:S1192")
    private static Map<String, String> buildNoInputPrompts()
    {
        Map<String, String> rc = new HashMap<>();
        rc.put(DEFAULT, "No speech detected. Please try again.");
        rc.put("fra-FRA", "Aucune dictée détectée. Veuillez réessayer.");
        rc.put("fra-CAN", "Aucune dictée détectée. Veuillez réessayer.");
        rc.put("por-BRA", "Nenhuma fala detectada. Por favor tente novamente.");
        rc.put("por-PRT", "Fala não detectada. Tente de novo.");
        rc.put("ita-ITA", "Nessun discorso rilevato. Prova di nuovo.");
        rc.put("deu-DEU", "Keine Aufnahme erkannt. Bitte versuchen Sie es erneut.");
        rc.put("spa-ESP", "No se detecta ninguna voz. Intente de nuevo.");
        rc.put("spa-XLA", "No se detecta ninguna voz. Intente de nuevo.");
        rc.put("spa-USA", "No se detecta ninguna voz. Intente de nuevo.");
        rc.put("cmn-CHN", "无法识别，请重试。");
        rc.put("cmn-TWN", "無法錄製語音，請重試。");
        rc.put("yue-CHN", "無法錄製語音，請重試。");
        rc.put("heb-ISR", "לא זוהה דיבור. בבקשה נסה שוב.");
        rc.put("jpn-JPN", "発話が聞き取れませんでした。もう一度試して下さい。");
        rc.put("swe-SWE", "Identifierade inget tal. Vänligen försök igen.");
        rc.put("rus-RUS", "Запись речи не обнаружена. Пожалуйста, попробуйте ещё раз.");
        rc.put("pol-POL", "Nie wykryto mowy. Spróbuj ponownie.");
        rc.put("hun-HUN", "Nincs felismerheto beszéd. Próbálja újra.");
        rc.put("fin-FIN", "Puhetta ei tunnistettu. Yritä uudelleen.");
        rc.put("ell-GRC", "Δεν εντοπίστηκε ομιλία. ΠΑΡΑΚΑΛΩ προσπαθησε ξανα.");
        rc.put("ces-CZE", "Nebyla zjištěna žádná řeč. Prosím zkuste to znovu.");
        rc.put("ron-ROU", "Nu a fost detectată nicio vorbire. Vă rugăm să încercați din nou.");
        rc.put("nor-NOR", "Ingen tale registrert. Prøv igjen.");
        rc.put("nld-NLD", "Geen spraak ontdekt. Probeer het opnieuw.");
        rc.put("zlm-MYS", "Tiada pertuturan dikesan. Sila cuba lagi.");
        rc.put("slk-SVK", "Nerozpoznala sa žiadna reč. Prosím skúste znova.");
        rc.put("ukr-UKR", "Мовлення не виявлено. Будь ласка спробуйте ще раз.");
        rc.put("hrv-HRV", "Govor nije otkriven. Molim te pokušaj ponovno.");
        rc.put("kor-KOR", "음성이 인식되지 않았습니다. 다시 시도해 주십시오.");
        rc.put("dan-DNK", "Der er ikke detekteret nogen tale. Prøv igen.");
        rc.put("ara-XWW", "لم يتم الكشف عن الكلام. حاول مرة اخرى.");
        rc.put("tur-TUR", "Konuşma algılanmadı. Lütfen tekrar deneyin.");
        rc.put("vie-VNM", "Không có giọng nói nào được phát hiện. Vui lòng thử lại.");
        rc.put("ind-IDN", "Tidak ada ucapan yang terdeteksi. Silakan coba lagi.");
        rc.put("tha-THA", "ไม่พบคำพูด กรุณาลองอีกครั้ง.");
        rc.put("cat-ESP", "No s'ha detectat cap parla. Siusplau torna-ho a provar.");
        return rc;
    }

    public static String getRecNoMatchPrompt(Map<String, Object> queryInfo)
    {
        String lang = getLangFromQueryInfo(queryInfo);
        return getRecNoMatchPrompt(lang);
    }

    public static String getRecNoMatchPrompt(String lang)
    {
        String rc = noMatchPrompts.get(lang);
        if(rc == null) rc = noMatchPrompts.get(DEFAULT);
        return rc;
    }

    public static String getRecNoInputTimeoutPrompt(Map<String, Object> queryInfo)
    {
        String lang = getLangFromQueryInfo(queryInfo);
        return getRecNoInputTimeoutPrompt(lang);
    }

    public static String getRecNoInputTimeoutPrompt(String lang)
    {
        String rc = noInputPrompts.get(lang);
        if(rc == null) rc = noInputPrompts.get(DEFAULT);
        return rc;
    }

    private static String getLangFromQueryInfo(Map<String, Object> queryInfo)
    {
        if(queryInfo.containsKey("dictation_language"))
        {
            return (String) queryInfo.get("dictation_language");
        } else {
            return (String) queryInfo.getOrDefault("language", "");
        }
    }
}

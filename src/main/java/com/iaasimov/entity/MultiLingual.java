package com.iaasimov.entity;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Language;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.LanguageListOption;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.iaasimov.workflow.GlobalConstantsNew;
import jdk.nashorn.internal.parser.JSONParser;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

/**
 * Created by USER on 24-04-2018.
 */
public class MultiLingual {
    public static String detectLanguage(String sourceText, PrintStream out) {
        Translate translate = createTranslateService();

        System.out.println("Translate object as received " + translate);


        Detection detection = translate.detect(sourceText);
        System.out.println("Language(s) detected:");
        out.printf("\t%s\n", detection.getLanguage());

        return detection.getLanguage();

    }

    /**
     * Translates the source text in any language to English.
     *
     * @param sourceText source text to be translated
     * @param out print stream
     */
    public static String translateText(String sourceText, PrintStream out) {
        Translate translate = createTranslateService();
        Translation translation = translate.translate(sourceText);
        out.printf("Source Text:\n\t%s\n", sourceText);
        out.printf("Translated Text:\n\t%s\n", translation.getTranslatedText());
        return translation.getTranslatedText();
    }

    /**
     * Translate the source text from source to target language.
     * Make sure that your project is whitelisted.
     *
     * @param sourceText source text to be translated
     * @param sourceLang source language of the text
     * @param targetLang target language of translated text
     * @param out print stream
     */
    public static void translateTextWithOptionsAndModel(
            String sourceText,
            String sourceLang,
            String targetLang,
            PrintStream out) {

        Translate translate = createTranslateService();
        TranslateOption srcLang = TranslateOption.sourceLanguage(sourceLang);
        TranslateOption tgtLang = TranslateOption.targetLanguage(targetLang);

        // Use translate `model` parameter with `base` and `nmt` options.
        TranslateOption model = TranslateOption.model("nmt");

        Translation translation = translate.translate(sourceText, srcLang, tgtLang, model);
        out.printf("Source Text:\n\tLang: %s, Text: %s\n", sourceLang, sourceText);
        out.printf("TranslatedText:\n\tLang: %s, Text: %s\n", targetLang,
                translation.getTranslatedText());
    }


    /**
     * Translate the source text from source to target language.
     *
     * @param sourceText source text to be translated
     * @param sourceLang source language of the text
     * @param targetLang target language of translated text
     * @param out print stream
     */
    public static void translateTextWithOptions(
            String sourceText,
            String sourceLang,
            String targetLang,
            PrintStream out) {

        Translate translate = createTranslateService();
        TranslateOption srcLang = TranslateOption.sourceLanguage(sourceLang);
        TranslateOption tgtLang = TranslateOption.targetLanguage(targetLang);

        Translation translation = translate.translate(sourceText, srcLang, tgtLang);
        out.printf("Source Text:\n\tLang: %s, Text: %s\n", sourceLang, sourceText);
        out.printf("TranslatedText:\n\tLang: %s, Text: %s\n", targetLang,
                translation.getTranslatedText());
    }

    /**
     * Displays a list of supported languages and codes.
     *
     * @param out print stream
     * @param tgtLang optional target language
     */
    public static void displaySupportedLanguages(PrintStream out, Optional<String> tgtLang) {
        Translate translate = createTranslateService();
        LanguageListOption target = LanguageListOption.targetLanguage(tgtLang.orElse("en"));
        List<Language> languages = translate.listSupportedLanguages(target);

        for (Language language : languages) {
            out.printf("Name: %s, Code: %s\n", language.getName(), language.getCode());
        }
    }

    /**
     * Create Google Translate API Service.
     *
     * @return Google Translate Service
     */
    public static Translate createTranslateService() {
        Translate t = null;
        try {

            // all of the code in IaaSimov will work via resource URL for consistency
            // Resource can be a file a DB, a tripe or anything else...
            //IaaSimov will deligate to Java for resource manipulation...
            
            InputStream s = Resources.getResource(GlobalConstantsNew.getInstance().iaaSimovMultilingual).openStream();
            GoogleCredentials credentials = GoogleCredentials.fromStream(s);
            t = TranslateOptions.newBuilder().setCredentials(credentials).build().getService();

            //// Code below is a redherring it works when we run from IDE like IntelliJ but fails when run from command line
            /// Reason is simple IDEs are smart and it convert the unnecessary characters that are can break the code

            //String path = Resources.getResource(GlobalConstantsNew.getInstance().iaaSimovMultilingual).getPath();
            //GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(new File(path)));


        }
        catch (Exception ex)
        {
            System.out.println(ex.getStackTrace());
        }
        //return TranslateOptions.newBuilder().build().getService();
        return t;
    }

}


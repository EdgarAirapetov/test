package com.numplates.nomera3.presentation.view.widgets.numberplateview.maskformatter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by maciek on 16.03.2016.
 * Created by artem on 07.06.18
 */
public class CharTransforms {

    private final Map<Character, TransformPattern> transformMap = new HashMap<>();

    public CharTransforms(String pattern){
        this.transformMap.put('0', new TransformPattern("[0-9]", false, false));
        this.transformMap.put('a', new TransformPattern(pattern, true, true));


    }

    private static class TransformPattern {

        private final Pattern pattern;
        private final boolean upperCase;
        private final boolean lowerCase;

        public TransformPattern(String pattern, boolean upperCase, boolean lowerCase) {
            this.pattern = Pattern.compile(pattern);
            this.upperCase = upperCase;
            this.lowerCase = lowerCase;
        }

        public char transformChar(char stringChar) throws InvalidTextException {
            char modified;
            if (upperCase) {
                modified = Character.toUpperCase(stringChar);
            } else if (lowerCase) {
                modified = Character.toLowerCase(stringChar);
            } else {
                modified = stringChar;
            }

            if (!pattern.matcher(modified + "").matches()) {
                throw new InvalidTextException();   //TODO: (Artem) а оно нам надо этот эксепшн? мож пропустить?
            }
            return modified;
        }

    }

     public char transformChar(char stringChar, char maskChar) throws InvalidTextException {
        TransformPattern transform = transformMap.get(maskChar);
        if (transform == null) {
            return stringChar;
        }

        return transform.transformChar(stringChar);
    }

}

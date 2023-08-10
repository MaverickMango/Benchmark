/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.codec.language.bm;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

/**
 * Tests PhoneticEngine and Languages.LanguageSet in ways very similar to code found in solr-3.6.0.
 *
 * @since 1.7
 */
public class PhoneticEngineRegressionTest {

    @Test
    public void testSolrGENERIC() {
        Map<String, String> args;

        // concat is true, ruleType is EXACT
        args = new TreeMap<String, String>();
        args.put("nameType", "GENERIC");
        assertEquals(encode(args, true, "Angelo"), "agilo|angilo|aniilo|anilo|anxilo|anzilo|ogilo|ongilo|oniilo|onilo|onxilo|onzilo");
        args.put("ruleType", "EXACT");
        assertEquals(encode(args, true, "Angelo"), "anZelo|andZelo|angelo|anhelo|anjelo|anxelo");
        assertEquals(encode(args, true, "D'Angelo"), "(anZelo|andZelo|angelo|anhelo|anjelo|anxelo)-(danZelo|dandZelo|dangelo|danhelo|danjelo|danxelo)");


        // concat is false, ruleType is EXACT
        args = new TreeMap<String, String>();
        assertEquals(encode(args, false, "Angelo"), "agilo|angilo|aniilo|anilo|anxilo|anzilo|ogilo|ongilo|oniilo|onilo|onxilo|onzilo");
        args.put("ruleType", "EXACT");
        assertEquals(encode(args, false, "Angelo"), "anZelo|andZelo|angelo|anhelo|anjelo|anxelo");
        assertEquals(encode(args, false, "D'Angelo"), "(anZelo|andZelo|angelo|anhelo|anjelo|anxelo)-(danZelo|dandZelo|dangelo|danhelo|danjelo|danxelo)");


        // concat is true, ruleType is APPROX
        args = new TreeMap<String, String>();
        assertEquals(encode(args, true, "Angelo"), "agilo|angilo|aniilo|anilo|anxilo|anzilo|ogilo|ongilo|oniilo|onilo|onxilo|onzilo");
        args.put("ruleType", "APPROX");
        assertEquals(encode(args, true, "Angelo"), "agilo|angilo|aniilo|anilo|anxilo|anzilo|ogilo|ongilo|oniilo|onilo|onxilo|onzilo");
        assertEquals(encode(args, true, "D'Angelo"), "(agilo|angilo|aniilo|anilo|anxilo|anzilo|ogilo|ongilo|oniilo|onilo|onxilo|onzilo)-(dagilo|dangilo|daniilo|danilo|danxilo|danzilo|dogilo|dongilo|doniilo|donilo|donxilo|donzilo)");


        // concat is false, ruleType is APPROX
        args = new TreeMap<String, String>();
        assertEquals(encode(args, false, "Angelo"), "agilo|angilo|aniilo|anilo|anxilo|anzilo|ogilo|ongilo|oniilo|onilo|onxilo|onzilo");
        args.put("ruleType", "APPROX");
        assertEquals(encode(args, false, "Angelo"), "agilo|angilo|aniilo|anilo|anxilo|anzilo|ogilo|ongilo|oniilo|onilo|onxilo|onzilo");
        assertEquals(encode(args, false, "D'Angelo"), "(agilo|angilo|aniilo|anilo|anxilo|anzilo|ogilo|ongilo|oniilo|onilo|onxilo|onzilo)-(dagilo|dangilo|daniilo|danilo|danxilo|danzilo|dogilo|dongilo|doniilo|donilo|donxilo|donzilo)");

    }

    @Test
    public void testSolrASHKENAZI() {
        Map<String, String> args;

        // concat is true, ruleType is EXACT
        args = new TreeMap<String, String>();
        args.put("nameType", "ASHKENAZI");
        assertEquals(encode(args, true, "Angelo"), "AnElO|AnSelO|AngElO|AngzelO|AnkselO|AnzelO");
        args.put("ruleType", "EXACT");
        assertEquals(encode(args, true, "Angelo"), "andZelo|angelo|anhelo|anxelo");
        assertEquals(encode(args, true, "D'Angelo"), "dandZelo|dangelo|danhelo|danxelo");


        // concat is false, ruleType is EXACT
        args = new TreeMap<String, String>();
        args.put("nameType", "ASHKENAZI");
        assertEquals(encode(args, false, "Angelo"), "AnElO|AnSelO|AngElO|AngzelO|AnkselO|AnzelO");
        args.put("ruleType", "EXACT");
        assertEquals(encode(args, false, "Angelo"), "andZelo|angelo|anhelo|anxelo");
        assertEquals(encode(args, false, "D'Angelo"), "dandZelo|dangelo|danhelo|danxelo");


        // concat is true, ruleType is APPROX
        args = new TreeMap<String, String>();
        args.put("nameType", "ASHKENAZI");
        assertEquals(encode(args, true, "Angelo"), "AnElO|AnSelO|AngElO|AngzelO|AnkselO|AnzelO");
        args.put("ruleType", "APPROX");
        assertEquals(encode(args, true, "Angelo"), "AnElO|AnSelO|AngElO|AngzelO|AnkselO|AnzelO");
        assertEquals(encode(args, true, "D'Angelo"), "dAnElO|dAnSelO|dAngElO|dAngzelO|dAnkselO|dAnzelO");


        // concat is false, ruleType is APPROX
        args = new TreeMap<String, String>();
        args.put("nameType", "ASHKENAZI");
        assertEquals(encode(args, false, "Angelo"), "AnElO|AnSelO|AngElO|AngzelO|AnkselO|AnzelO");
        args.put("ruleType", "APPROX");
        assertEquals(encode(args, false, "Angelo"), "AnElO|AnSelO|AngElO|AngzelO|AnkselO|AnzelO");
        assertEquals(encode(args, false, "D'Angelo"), "dAnElO|dAnSelO|dAngElO|dAngzelO|dAnkselO|dAnzelO");

    }

    @Test
    public void testSolrSEPHARDIC() {
        Map<String, String> args;

        // concat is true, ruleType is EXACT
        args = new TreeMap<String, String>();
        args.put("nameType", "SEPHARDIC");
        assertEquals(encode(args, true, "Angelo"), "anhila|anhilu|anzila|anzilu|nhila|nhilu|nzila|nzilu");
        args.put("ruleType", "EXACT");
        assertEquals(encode(args, true, "Angelo"), "anZelo|andZelo|anxelo");
        assertEquals(encode(args, true, "D'Angelo"), "anZelo|andZelo|anxelo");


        // concat is false, ruleType is EXACT
        args = new TreeMap<String, String>();
        args.put("nameType", "SEPHARDIC");
        assertEquals(encode(args, false, "Angelo"), "anhila|anhilu|anzila|anzilu|nhila|nhilu|nzila|nzilu");
        args.put("ruleType", "EXACT");
        assertEquals(encode(args, false, "Angelo"), "anZelo|andZelo|anxelo");
        assertEquals(encode(args, false, "D'Angelo"), "danZelo|dandZelo|danxelo");


        // concat is true, ruleType is APPROX
        args = new TreeMap<String, String>();
        args.put("nameType", "SEPHARDIC");
        assertEquals(encode(args, true, "Angelo"), "anhila|anhilu|anzila|anzilu|nhila|nhilu|nzila|nzilu");
        args.put("ruleType", "APPROX");
        assertEquals(encode(args, true, "Angelo"), "anhila|anhilu|anzila|anzilu|nhila|nhilu|nzila|nzilu");
        assertEquals(encode(args, true, "D'Angelo"), "anhila|anhilu|anzila|anzilu|nhila|nhilu|nzila|nzilu");


        // concat is false, ruleType is APPROX
        args = new TreeMap<String, String>();
        args.put("nameType", "SEPHARDIC");
        assertEquals(encode(args, false, "Angelo"), "anhila|anhilu|anzila|anzilu|nhila|nhilu|nzila|nzilu");
        args.put("ruleType", "APPROX");
        assertEquals(encode(args, false, "Angelo"), "anhila|anhilu|anzila|anzilu|nhila|nhilu|nzila|nzilu");
        assertEquals(encode(args, false, "D'Angelo"), "danhila|danhilu|danzila|danzilu|nhila|nhilu|nzila|nzilu");

    }

    @Test
    public void testCompatibilityWithOriginalVersion() {
        // see CODEC-187
        // comparison: http://stevemorse.org/census/soundex.html

        Map<String, String> args = new TreeMap<String, String>();
        args.put("nameType", "GENERIC");
        args.put("ruleType", "APPROX");

        assertEquals(encode(args, true, "abram"), "Ybram|Ybrom|abram|abran|abrom|abron|avram|avrom|obram|obran|obrom|obron|ovram|ovrom");
        assertEquals(encode(args, true, "Bendzin"), "bndzn|bntsn|bnzn|vndzn|vntsn");

        args.put("nameType", "ASHKENAZI");
        args.put("ruleType", "APPROX");

        assertEquals(encode(args, true, "abram"), "Ybram|Ybrom|abram|abrom|avram|avrom|imbram|imbrom|obram|obrom|ombram|ombrom|ovram|ovrom");
        assertEquals(encode(args, true, "Halpern"), "YlpYrn|Ylpirn|alpYrn|alpirn|olpYrn|olpirn|xalpirn|xolpirn");

    }

    /**
     * This code is similar in style to code found in Solr:
     * solr/core/src/java/org/apache/solr/analysis/BeiderMorseFilterFactory.java
     *
     * Making a JUnit test out of it to protect Solr from possible future
     * regressions in Commons-Codec.
     */
    private static String encode(Map<String, String> args, boolean concat, String input) {
//        Languages.LanguageSet languageSet;
        PhoneticEngine engine;

        // PhoneticEngine = NameType + RuleType + concat
        // we use common-codec's defaults: GENERIC + APPROX + true
        String nameTypeArg = args.get("nameType");
        NameType nameType = (nameTypeArg == null) ? NameType.GENERIC : NameType.valueOf(nameTypeArg);

        String ruleTypeArg = args.get("ruleType");
        RuleType ruleType = (ruleTypeArg == null) ? RuleType.APPROX : RuleType.valueOf(ruleTypeArg);

        engine = new PhoneticEngine(nameType, ruleType, concat);

        /*
            org/apache/lucene/analysis/phonetic/BeiderMorseFilter.java (lines 96-98) does this:

            encoded = (languages == null)
                ? engine.encode(termAtt.toString())
                : engine.encode(termAtt.toString(), languages);

            Hence our approach, below:
        */
        return engine.encode(input);
    }
}

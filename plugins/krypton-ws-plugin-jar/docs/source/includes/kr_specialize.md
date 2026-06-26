# Reference topics 

This section provides information about data packs and how to customize them for specific businesses or environments.  

## Data pack

> Data pack includes acoustic and language model

> ![Data pack](dp.png)

In order to recognize and transcribe user speech, Krypton uses a data pack plus optional dynamic content. The data pack provides general information about a language or locale, while optional resources such as domain language models and wordsets specialize Krypton's recognition abilities for a particular application or environment. 

Krypton always works with one or more factory data packs, available in several languages and locales. The data pack includes these neural network-based models:

- *Acoustic model* (AM) translates utterances into phonetic representations of speech.   

- *Language model* (LM) identifies the words or phrases most likely spoken.

The base acoustic model is trained to give good performance in many acoustic environments. The base language model is developed to remain current with popular vocabulary and language use. As such, Krypton paired with a data pack is ready for use out-of-the-box for many applications.

### Data pack readme

> README file for American English

```plaintext
Core Technologies 4.0 English - United States Data Pack 4.3.0

Language code: eng-USA
Topic: GEN
Supported builtins: AMOUNT BOOLEAN CALENDARX CARDINAL_NUMBER DISTANCE DOUBLE 
DURATION DURATION_RANGE GENERIC_ORDER GLOBAL ORDINAL_NUMBER QUANTITY_REL 
TEMPERATURE ALPHANUM DATE DIGITS NUMBERS TIME VERT_FINANCIAL_SERVICES 
VERT_HEALTHCARE VERT_TRAVEL VERT_TELECOMMUNICATIONS
Supported speech domains: messaging ivr transcription
Supported formattings: phone date num_as_digits time default address all_as_words
Supported formatting parameters: format_addresses format_phone_numbers 
censor_full_words format_non-USA_postcodes format_dates abbreviate_titles
two_spaces_after_period million_as_numerals format_currency_codes format_prices 
censor_profanities abbreviate_units improper_fractions_as_numerals format_times 
mixed_numbers_as_numerals format_URLs_and_email_addresses expand_contractions 
format_social_security_numbers
Supported sampling rates: 8000 16000
Supported audio encodings: linear pcm, a-law, mu-law
```

Most data packs include a README file that lists the supported features. The README file is installed with the data pack, for example in *$NDP_HOME (/usr/local/Nuance/Data_Packs)/ndp-eng-USA-GEN-4.3.0/ndp-eng-USA-GEN-4.3.0_README.txt*. 

Possible features for the data pack are:

| Feature                         | Description                                                  |
| ------------------------------- | ------------------------------------------------------------ |
| Language&#160;code              | The underlying locale code of the data pack.                 |
| Topic                           | The main identifier for a data pack, for example GEN for general. |
| Supported builtins              | Slices of the base language model that may be weighted individually, for example ALPHANUM, AMOUNT, BOOLEAN, and so on. These items may be activated and weighted in [Recognize](#recognizel) commands. |
| Supported speech domains        | Weightset mappings supported by the data pack, for example: ivr, healthcare, messaging, financial_services, and so on. These speech domains may be specified in the speechDomain [recognition parameter](#recognition-parameters). |
| Supported formattings           | Preset text formatting options supported by the data pack, for example: phone, date, num_as_digits, time, default, address, all_as_words. These formats may be specified in the formattingType [recognition parameter](#recognition-parameters). See [Formatting](#formatting). |
| Supported formatting parameters | Low-level text formatting options supported by the data pack, for example abbreviate_titles, censor_profanities, and so on. These formats may be specified in the formattingParams [recognition parameter](#recognition-parameters). See [Formatting](#formatting). |
| Supported sampling rates        | Whether narrowband 8kHz (8000) and/or wide band 16kHz (16000) are supported. Some data packs support both 8000 and 16000 sample rates. |
| Supported audio encodings       | The audio formats supported by the data pack. Krypton can deal with all supported audio codecs: linear 8khz/16khz, µ-law and A-law. When the codec is not listed in the data pack README, audio is adjusted accordingly. Best results are obtained, however, when audio is natively recorded and provided in the native encoding listed here. |

Some data packs include a file named *external-phonesets.tsv* in the lm/ directory of the data pack. This file contains the symbols used for the phonetic alphabets supported by Krypton for custom pronunciation. See [Custom pronunciations](#custom-pronunciations). 

### Specializing the data pack

You may extend the data pack at runtime using several types of dynamic specialization objects: 

- [Builtins](#builtins)   
- [Domain LMs](#domain-lms)  
- [Wordsets](#wordsets)   
- [Speaker profiles](#speaker-profiles)  

Each recognition turn can leverage a weighted mix of builtins, domain LMs, wordsets, and speaker profiles.

## Builtins 

> Data pack builtins

```python
{  
  "method":"Recognize",
  "sessionId":"xxxx from CreateSession",
  "builtinWeights":{  
    "AMOUNT":"high",
    "DATE":"medium"
  },
  ...
}
```

The data pack may include one or more *builtins*, or slices of the language pack focused on common tasks (numbers, dates, and so on) or general information in a vertical domain such as financial services or healthcare. 

To use a builtin in Krypton, activate and give it a weight in [Recognize](#recognize-and-interpret) - builtinWeights. The weight is a keyword (lowest, low, medium, high, highest) or an integer 0-1000. See also [Resource weights](#resource-weights). 

The available builtins depend on the data pack and are listed in the [data pack readme](#data-pack-readme) file. For American English data packs, for example, the builtins are:

<pre class="center-column2"><code>ALPHANUM           DOUBLE            TEMPERATURE
AMOUNT             DURATION          TIME
BOOLEAN            DURATION_RANGE    VERT_FINANCIAL_SERVICES
CALENDARX          GENERIC_ORDER     VERT_HEALTHCARE
CARDINAL_NUMBER    GLOBAL            VERT_TELECOMMUNICATIONS
DATE               NUMBERS           VERT_TRAVEL
DIGITS             ORDINAL_NUMBER
DISTANCE           QUANTITY_REL</code>
</pre>

## Domain LMs

> Two DLMs with entities

> ![Domain LMs](dlms.png)

Each data pack supplied with Krypton provides a base language model that lets Krypton recognize the most common terms and constructs in the language and locale. 

You may complement this language model by creating one or more domain-specific models, called *domain language models* (*domain LMs* or *DLMs*). Each DLM is based on sentences from a specific environment, or domain, and may include *entities*, or collections of terms used in that environment. 

In Krypton, a DLM is a recognition object loaded into a session with CreateSession or Load, then optionally activated in Recognize.

Krypton accepts up to ten DLMs, which are weighted along with other recognition objects. See [Resource weights](#resource-weights). 

## Wordsets

> Wordsets extend entities in DLMs

> ![Wordset](wordsets.png)

> Different ways to define wordsets: with zip file, JSON file, or inline

``` json 
"objects" | "sessionObjects" [
  {"id": "ws_places",
   "type": "application/x-nuance-wordset-pkg",
   "url": "urn:..." 
  },
  {"id": "ws_places2",
   "type": "application/x-nuance-wordset",
   "body": "{\"PLACES\":[{\"literal\":\"Abington Pigotts\"},...]}"
  }
]
```

A wordset is a collection of words that extends Krypton's recognition vocabulary by providing additional values for entities (also called concepts) in a DLM. For example, a wordset might provide the names in a user’s contact list or local place names. 

Each wordset is loaded into the session, along with its associated DLM, in a [CreateSession](#createsession) or [Load](#load) command. Wordsets do not have a specific weight and take the weight of their underlying DLM. See [Resource weights](#resource-weights). 

Using wordsets in Krypton requires three items:

- A wordset file containing extra values for specific entities, for example *place-names.json* or *contact-names.json*.
- An entity within a DLM to which the wordset is attached, for example PLACES or CONTACTS. 
- A DLM containing the entity, for example *sales-dlm.zip* or *banking-dlm.zip*. 

### Wordset JSON syntax

The wordset is defined in JSON format as a one or more arrays. Each array is named after an *entity* (a concept or category) defined within a DLM to which words can be added at runtime. Entities are templates that tell Krypton how and where words are used in a conversation.

For example, you might have an entity, CONTACTS, containing personal names, or PLACES, with place names used by the application. The wordset adds to the existing terms in the entity, but applies only to the current session or recognition turn. The terms in the wordset are not added permanently to the entity. All entities must be defined in DLMs, which are loaded and activated along with the wordset.

> This wordset adds terms to the PLACES entity

```json
{
  "PLACES" : [
    {"literal" : "La Jolla", 
     "spoken" : ["la hoya","la jolla"] 
    },
    {"literal" : "Beaulieu", 
     "spoken": ["bewly"] 
    },
    {"literal" : "Worcester", 
     "spoken": ["wuster","worcester"] 
    },
    {"literal" : "Abington Pigotts"
    },
    {"literal" : "Steeple Morden"
    }
  ]
}
```

The wordset includes additional values for one or more entities. The syntax is:

`{`  
&#160;&#160;`"<entity>" : [ `  
&#160;&#160;&#160;&#160;`{"literal": "<written_form>"`  
&#160;&#160;&#160;&#160;&#160;&#160;`"spoken": "<spoken_form_1, spoken_form_n>" `   
&#160;&#160;&#160;&#160;`}, `  
&#160;&#160;&#160;&#160;`{"literal": "<written_form>"`  
&#160;&#160;&#160;&#160;&#160;&#160;`"spoken": "<spoken_form_1, spoken_form_n>"`  
&#160;&#160;&#160;&#160;`},`  
&#160;&#160;&#160;&#160;`...`  
&#160;&#160;`"<entity>" : [...] `  
`}`


| Syntax   |        |                                                              |
| -------- | ------ | ------------------------------------------------------------ |
| *entity* | String | An entity (or concept) defined in a DLM, containing a set of values. The name is case-sensitive.  Consult the DLM training data for entity names. |
| literal  | String | The written form of the value to be returned by the Krypton formatted field. |
| spoken   | Array  | (Optional) One or more spoken forms of the value. When not supplied, Krypton guesses the pronunciation of the word from the literal. Include a spoken form only if the literal is difficult to pronounce or has an unusual pronunciation in the language.</p><p>When a spoken form is supplied, it is the only source for recognition: the literal is not considered. If the literal pronunciation is also valid, you should include it as a spoken form. </p><p>For example, the city of Worcester, Massachusetts is pronounced *wuster*, but users reading it on a map may say it literally, as *worcester*. To allow Krypton to recognize both forms, specify:</p><p>{"literal":"Worcester","spoken":["wuster","worcester"]}</p> |

### Wordset file

> Wordset file: CreateSession loads wordset along with its DLM. The DLM has a weight but not the wordset.

``` json
{
  "method": "CreateSession",
  ...
  "sessionObjects": [
    {"id": "dlm_sales",
     "type": "application/x-nuance-domainlm",
     "url": "urn:...",
     "weight": 200
    },
    {"id": "ws_places",
     "type": "application/x-nuance-wordset",
     "body": "{inline wordset definition}"
    }
  ]
}
```

When a wordset is provided in a single JSON file, the object is defined as x-nuance-wordset. For example, a CreateSession command can load the wordset *place-names.json* along with the DLM containing the PLACES entity that the wordset extends. 

The url points either to a remote file (http(s)://) or a local file (file://) The file itself is a text file containing the wordset in JSON format as described above. The text file may have any extension but .*json* is recommended for clarity.

### Inline wordset

> Inline wordset: Load command defines wordset within the command

``` json
{  
  "method": "Load",
  "sessionId": "xxxx from CreateSession",
  "objects": [  
    {"id": "ws_places2",
     "type": "application/x-nuance-wordset",
     "body": "{\"PLACES\":[{\"literal\":\"Fordoun\",\"spoken\":[\"forden\"]},
              {\"literal\":\"Auchenblae\"}]}"
    }
  ]
}
```

When a wordset is provided inline, the object is also defined as x-nuance-wordset, but the wordset JSON definition is provided within the command’s body field. The definition is a string value and requires appropriate escape sequences. 

### Wordset package

> Wordset package 

``` json
 "type": "application/x-nuance-wordset-pkg",
 "url": "urn:..."
```

To improve the performance of a large wordset at runtime, you may instead provide it as a precompiled package within a ZIP file.

When a wordset is provided in a ZIP file, the object is defined as x-nuance-wordset-**pkg**. 

The url points to a remote file (http(s)://) or a local file (file://). The ZIP package includes a JSON file containing the wordset, plus other files (.*grxml*, .*voc*, and *.ini*) used by Krypton.

## Speaker profiles

>  Enable speaker profiles in CreateSession

```json
{
  "method": "CreateSession",
  "sessionObjects": [
    {
     "type": "application/x-nuance-speaker-profile"
    }
  ]
}
```

Speaker adaptation is a technique that adapts the acoustic model and improves speech recognition based on qualities of the speaker and channel. The best results are achieved by updating the data pack’s acoustic model in real time based on the immediate utterance.

| Property    | Type   | Value                                                        |
| ----------- | ------ | ------------------------------------------------------------ |
| type        | String | Content type of the object: application/x-nuance-speaker-profile |
| url         | String | The endpoint, bucket, and file for storing speaker profiles in the format: http[s]://*endpoint*:*port*/*bucket*/*file*<ul><li>*endpoint*: The endpoint as  http[s]://*host*:*port*</li><li>*bucket*: The name of a bucket created as in step 2 above.</li><li>*file*: The name of a file in the bucket that Krypton will use to load/store speaker adaptation data. When loading, if this file does not exist, Krypton generates a new speaker profile that can be saved to this file.<br />Whether the data is saved in this file after a session depends on the enableSpeakerProfileUpdate parameter set in config, CreateSession, Recognize, or EndSession. The value in the sample config is true.</li></ul> |
| maxAge etc. |        | Optional cache control and timeout options properties (maxAge, maxStale, minFresh, etc.). These properties are applied when Krypton makes requests to endpoints. |

Once a speaker profile is enabled in CreateSession, it is in effect for the entire session, in every Recognize command. The returned ID cannot be used and should be ignored.

## Resource weights

> Resources used in recognition
>
> ![Resource weight](weight1.png)

> A wordset, two DLMs, and one builtin are declared in this example, leaving the base LM with a weight of 200
>
> ![Weight example](weight2-ws.png)

In each recognition turn, Krypton uses a weighted mix of resources: the base language model plus any builtins, DLMs, and wordsets declared in the recognition request. You may set specific weights for DLMs and builtins. You cannot set a weight for wordsets.

The total weight of all resources is 1000, made up of these components:  

| Component                 | Weight                                                       |
| ------------------------- | ------------------------------------------------------------ |
| [Base LM](#data-pack)     | <p>By default, the base language model has a weight of 1000 minus other components in the recognition turn, with a minimum of 100. If other resources exceed 900, their weight is reduced to allow the base LM a minimum weight of 100.</p><p>When [recognitionParameters](#recognition-parameters) - allowZeroBaseLmWeight is true, other resources may use the entire weight, with the base LM reduced to zero. In this case, the words in the base LM are still recognized, but with lower probability than words in the DLMs and other resources.</p> |
| [Builtins](#builtins)     | <p>The default weight of each declared builtin is 250. You set a weight for a builtin as you activate it in [Recognize](#recognize-and-interpret).</p> |
| [Domain LMs](#domain-lms) | <p>The default weight of each declared DLM is 250. You may set a weight for each DLM in</p><ul><li>[CreateSession](#createsession) - sessionObjects - weight</li><li>Load - objects - weight</li><li>Recognize - domainLmWeights.</li></ul> |
| [Wordsets](#wordsets)     | <p>The weight of each wordset is tied to the weight of its DLM. You cannot set a weight for wordsets.</p><p>Wordsets also have a small fixed weight (100) to ensure clarity within the wordset, so values such as John Smith and Jon Taylor are not confused as John Taylor and Jon Smith. This weight applies to all wordsets together.<p> |

If you wish to emphasize the DLM at the expense of the base LM, give it a weight of 1000 and turn on allowZeroBaseLmWeight. 

## Formatting

> Formatted vs. minimally formatted text

```json
"formattedText":"We’re expecting 10 cm overnight",
"minimallyFormattedText":"We’re expecting ten centimeters overnight",
```

Krypton returns its transcription in two fields in the [PartialResult](#partialresult) and [FinalResult](#finalresult) events: formattedText and minimallyFormattedText. 

- Formatted text includes initial capitals for recognized names and places, numbers expressed as digits, currency symbols, and common abbreviations. 
- In minimally formatted text, words are spelled out but basic capitalization and punctuation are included.

In many cases, both formats are identical.

Krypton uses the settings in the data pack to format the material in formattedText (for example displaying "ten centimeters" as "10 cm") but you may adjust some  of these settings using the recognition parameters formattingType and formattingParams. 

### formattingType 

> formattingType

```json
  "recognitionParameters":{  
    "autoEnd":false,
    "enablePartialResults":true,
    "immutablePartialResults":true,
    "formattingType":"date"
  }
```

The formattingType determines how ambiguous numbers are displayed in the formattedText field. Only one type may be specified, for example `"formattingType": "date"`

Each data pack lists the supported types in its [readme file](#data-pack-readme) (Supported formattings) or from a [GetInfo](#getinfo) command (formatting_config). For most data packs, the types are date, time, phone, address, all_as_words, default, and num_as_digits. 

Each formattingType is a collection of many parameters (see [formattingParams](#formattingparams) below), but the defining parameter is PatternBias, which sets the preferred pattern for numbers that cannot otherwise be interpreted. The values of PatternBias give their name to the date, time, phone, address, and default formattingTypes. 

The PatternBias parameter cannot be modified, but you may adjust the other parameters individually using formattingParams. 

#### date, time, phone, and address

> formattingType helps Krypton interpret ambiguous numbers, e.g. "It's seven twenty six"

```plaintext
"formattingType": "date"    -->  7/26
"formattingType": "time"    -->  7:26
"formattingType": "address" -->  726
"formattingType": "phone"   -->  726 
```

The formattingTypes date, time, phone, and address tell Krypton to prefer one pattern for ambiguous numbers. 

Krypton can identify some numbers as date or time or phone number, for example:

- “I’ll catch the <u>seven twenty six a m</u> train” is identified as a time because of “a m.”
- “I was born on <u>eleven twenty six nineteen ninety four</u>” is identified as a date (in American English) because of the sequence of month, day, and year. 
- “It's <u>six nine seven three two nine four</u>” is identified as a phone number because of the pattern of the numbers.  

But Krypton considers some numbers ambiguous:

- “I’ll catch the <u>seven twenty six</u> train” is not recognized as a specific pattern, so Krypton displays it as a simple cardinal number: “I’ll catch the 726 train.” 

- “My birthday is <u>eleven twenty six</u>.” Similarly, Krypton displays this as: “My birthday is 1126.” 

By setting formattingType to date, time, phone, or address, you instruct Krypton to interpret these ambiguous numbers as the specified pattern. For example, if you know that the utterances coming into your application are likely to contain dates rather than times, set `formattingType: date`. 

#### all_as_words

> formattingType all_as_words

```plaintext
"I'll catch the seven twenty six a m train"

With "formattingType": "all_as_words" 
-->  I'll catch the seven twenty six a.m. train

With any other formattingType
--> I'll catch the 7:26 AM train
```

The all_as_words formattingType displays all numbers as words, even when a pattern (date, time, phone, or address) is found.  For example, Krypton identifies this as an address: “My address is seven twenty six brookline avenue cambridge mass oh two one three nine.” 

- With formattingType all_as_words, the numbers are written out and address formatting is ignored: "My address is seven twenty six Brookline Avenue, Cambridge, Mass. Oh two one three nine"  
- With all other types, the text is formatted as a standard address: "My address is 726 Brookline Ave., Cambridge, MA 02139"

#### num_as_digits

The num_as_digits formattingType is the same as default, except in its treatment of numbers under 10. Default formats numbers as numerals from 10 upwards (one, two, three ... nine, 10, 11, 12, etc.), while num_as_digits formats numbers as numerals from 0 (1, 2, 3, etc.). 

Num_as_digits affects isolated cardinal and ordinal numbers, plural cardinals (ones, twos, nineteen fifties, etc.), some prices, and fractions. "Isolated" means a number that is not found within a greater pattern such as date or time. 

#### default

This formattingType is the default. It has the same effect as not specifying a formattingType. If Krypton cannot determine the format of the number, it interprets it as a cardinal number. 

<a name="katakana-scheme"></a>

#### all_as_katakana

formattingType all_as_katakana

```plaintext
Japanese form of "How many kilograms can I check in?"

With "formattingType": "all_as_katakana"  
--> アズケルニモツノオモサハナンキロマデデスカ

With the default or any other type
-->  預ける荷物の重さは何キロまでですか
```

Available for Japanese data packs only, the formattingType all_as_katakana returns the transcription in Katakana, meaning the output is entirely in the phonetic Katakana script, without Kanji, Arabic numbers, or Latin characters. 

When all_as_katakana is not specified, the output is a mix of scripts representing standard written Japanese.

This type has no modifiable options. 

### formattingParams

> No formattingType or formattingParams: formattingType default is in effect

```json
  "recognitionParameters":{  
    "autoEnd":false,
    "enablePartialResults":true,
    "immutablePartialResults":true,
  }
```

> formattingType only: all parameters in formattingType date are in effect

```json
  "recognitionParameters":{  
    "autoEnd":false,
    "enablePartialResults":true,
    "immutablePartialResults":true,
    "formattingType":"date",
  }
```

> formattingParams only: parameters in formattingType default overridden by specific formattingParams

```json
  "recognitionParameters":{  
    "autoEnd":false,
    "enablePartialResults":true,
    "immutablePartialResults":true,
    "formattingParams":{
      "abbreviate_titles":true,
      "abbreviate_units":false,
      "censor_full_words":true
    }
  }
```

> Both: parameters as in formattingType date overridden by formattingParams

```json
  "recognitionParameters":{  
    "autoEnd":false,
    "enablePartialResults":true,
    "immutablePartialResults":true,
    "formattingType":"date",
    "formattingParams":{
      "abbreviate_titles":true,
      "abbreviate_units":false,
      "censor_profanities":true
    }
  }
```

The formattingParams are individual parameters for displaying words and numbers in the formattedText result field. All  parameters are part of the current formattingType (default if not specified) but can be set on their own to override the current setting. The num_as_digits and all_as_katakana formattingTypes have no modifiable parameters.

These are the parameters supported by all Krypton languages. To see which ones apply to your language, consult the [data pack readme](#data-pack-readme) file (Supported formatting parameters) or the [GetInfo](#getinfo) result  (formatting_params).

 All parameters are boolean. The values are set in the formattingType to which they belong. 

<table>
<thead>
  <tr>
    <th rowspan="2">formattingParams</td>
    <th colspan="2" style="text-align:center">formattingType</td>
  </tr> 
  <tr>
    <th>default,&#160;date,&#160;time, phone,&#160;address</td>
    <th>all_as_words</td>
  </tr>
</thead>
<tbody>
  <tr>
    <td><code>PatternBias</code><br />The defining characteristic of the scheme. Not modifiable.</td>
    <td>default, date, time, phone, address</td>
    <td>&#160;</td>
  </tr>
  <tr>
    <td><code>abbreviate_titles</code><br />Whether to abbreviate titles such as Captain (Capt), Director (Dir), Madame (Mme), Professor (Prof), etc. In American English, a period follows the abbreviation. The titles Mr, Mrs, and Dr are always abbreviated.</td>  
    <td>False</td>  
    <td>False</td>  
  </tr>
  <tr>
    <td><code>abbreviate_units</code><br />Whether to abbreviate units of measure such as centimeters (cm), meters (m), megabytes (MB), pounds (lbs), ounces (oz), miles per hour (mph), etc. When true, metric units are always abbreviated, but imperial one-word tokens are not abbreviated, so ten feet is 10 feet and twelve quarts is 12 quarts. The formatting of expressions with mutiple units depends on the units involved: only common combinations are formatted.</td>  
    <td>True</td>  
    <td>False</td>  
  </tr>
  <tr>
    <td><code>Arabic_numerals_not_Kanji</code> (Japanese)<br />How to display numbers. <br />False: All numbers are displayed in Kanji. <br />True: Numbers are either Arabic or half-formatted, depending on the half-formatted (million_as_numerals) setting. <br />By default, cardinals are half-formatted, meaning that magnitude words (thousands, millions, etc.) are in Kanji. See <a href="#japanese-options">Japanese options</a>.</td>  
    <td>True</td>  
    <td>False</td>  
  </tr>
  <tr>
    <td><code>capitalize_2nd_person_pronouns</code> (German)<br />Whether to capitalize second person personal pronouns such as Du, Dich, etc.</td>  
    <td>False</td>  
    <td>False</td>  
  </tr>
  <tr>
    <td><code>capitalize_3rd_person_pronouns</code> (German)<br />Whether to capitalize third-person personal pronouns such as Sie, Ihnen, etc.</td>  
    <td>True</td>  
    <tdTrue</td>  
  </tr>
  <tr>
    <td><code>censor_full_words</code><br />Whether to mask profanities completely with asterisks, for example "********" versus "frigging."</td>  
    <td>False</td>  
    <td>False</td>  
  </tr>
  <tr>
    <td><code>censor_profanities</code><br />Whether to mask profanities partially with asterisks, for example “fr*gging” versus “frigging.”</td>  
    <td>False</td>  
    <td>False</td>  
  </tr>
  <tr>
    <td><code>expand_contractions</code><br />In English data packs, whether to expand common contractions, for example "don't" versus "do not" or "it's nice" versus "it is nice."</td>  
    <td>False</td>  
    <td>False</td>  
  </tr>
  <tr>
    <td><code>format_addresses</code><br />Whether to format text identified as postal addresses. This does not include adding commas or new lines. Full street address formatting is done for most data packs, following the standards of the country's postal service.</td> 
    <td>True</td>  
    <td>False</td>  
  </tr>  
  <tr>
    <td><code>format_currency_codes</code><br />When format_prices is true, whether to replace the currency symbol with its ISO currency code, for example 125USD instead of $125.</td> 
    <td>False</td>  
    <td>False</td>  
  </tr> 
  <tr>
    <td><code>format_dates</code><br />Whether to format text identified as dates as, for example, 7/26/1994, 7/26/94, or 7/26. The order of month and day depends on the data pack.</td>  
    <td>True</td>  
    <td>False</td>  
  </tr>  
  <tr>
    <td><code>format_non-USA_postcodes</code><br />For non-US data packs, whether to format UK and Canadian postcodes. UK postcodes have the form A9 9AA, A99 9AA, etc. Canadian postal codes have the form A9A 9A9.</td>  
    <td>False</td>  
    <td>False</td>  
  </tr> 
  <tr>
    <td><code>format_phone_numbers</code><br />For US and Canadian data packs, whether to format numbers identified as phone numbers, as 123-456-7890 or 456-7899, optionally with 1 or +1 before the number.</td>  
    <td>True</td>  
    <td>False</td>  
  </tr>
  <tr>
    <td><code>format_prices</code><br />Whether to format numbers identified as prices, including currency symbols and price ranges. The currency symbol depends on the data pack language.</td>  
    <td>True</td>  
    <td>False</td>  
  </tr>
  <tr>
    <td><code>format_social_security_numbers</code><br />Whether to format numbers identified as US social security numbers or (for Canadian data packs) Canadian social insurance numbers. Both are a series of nine digits formatted as 123-45-6789 or 123 456 789.</td>  
    <td>False</td>  
    <td>False</td>  
  </tr>
  <tr>
    <td><code>format_times</code><br />Whether to format numbers identified as times (including both 12- and 24-hour times) as, for example, 10:35 with optional AM or PM.</td>  
    <td>True</td>  
    <td>False</td> 
  </tr>
  <tr>
    <td><code>format_URLs_and_email_addresses</code><br />Whether to format web and email addresses, including @ (for at) and most suffixes, including multiple suffixes, for example .ac.edu. Numbers are displayed as digits and output is in lowercase.</td>  
    <td>True</td>  
    <td>False</td>  
  </tr>
  <tr>
    <td><code>format_USA_phone_numbers</code> (Mexican)<br />Whether to use US phone formatting instead of Mexican.</td>  
    <td>False</td>  
    <td>False</td>  
  </tr>
  <tr>
    <td><code>improper_fractions_as_numerals</code><br />Whether to express improper fractions as numbers, for example 5/4 versus five fourths.</td>  
    <td>True</td>  
    <td>False</td>  
  </tr>
  <tr>
    <td><code>million_as_numerals</code><br />Whether to half-format numbers ending in million, billion, trillion, and so on, for example 5 million. See <a href="#japanese-options">Japanese options</a>.</td>  
    <td>True</td>  
    <td>Inactive</td>  
  </tr>
  <tr>
    <td><code>mixed_numbers_as_numerals</code><br />Whether to express numbers that are a combination or an integer and a fraction (three and a half) as numerals (3 1/2).</td>  
    <td>True</td>  
    <td>False</td>  
  </tr>
  <tr>
      <td><code>two_spaces_after_period</code><br />Whether to insert two spaces (instead of one) following a period (full stop), question mark, or exclamation mark.</td>  
    <td>False</td>  
    <td>False</td>  
  </tr>                        
  </tbody>
</table>


### Japanese options

> Formatting parameters in Japanese data packs

```plaintext
format_addresses
censor_full_words  
censor_profanities
abbreviate_units
format_phone_numbers
Arabic_numerals_not_Kanji
format_times
format_dates
million_as_numerals
format_URLs_and_email_addresses
format_prices
```

> Combining options: This displays all numbers in Kanji

```python
  "recognitionParameters":{  
    ...
    "formattingParams":{
      "Arabic_numerals_not_Kanji":false
    }
  }
```

> This displays numbers in Kanji and Arabic. This is the default setting so may be omitted. 

```python
  "recognitionParameters":{  
    ...
    "formattingParams":{
      "Arabic_numerals_not_Kanji":true,
      "millions_as_numerals":true
    }
  }
```

> This displays all numbers in Arabic

```python
  "recognitionParameters":{  
    ...
    "formattingParams":{
      "Arabic_numerals_not_Kanji":true,
      "millions_as_numerals":false
    }
  }
```

Japanese data packs support the formatting parameters shown at the right.  (See also <a href="#katakana-scheme">all_as_katakana</a> for a Japanese-specific formatting type.) In these data packs, two options work together to specify how numbers are displayed.

- `Arabic_numerals_not_Kanji` determines whether numbers are shown in Arabic, Kanji, or both.   

  For words containing numbers, the formatting output depends on whether the word is defined in the system. For example, 八百屋, which means greengrocer, is a defined word. Even when Arabic_numerals_not_Kanji is True, it is always output as 八百屋, never as 800屋.   

  If the word containing a number is not defined in the system, the formatting output depends on the context and the formatting scheme in effect (date, time, price, address, and so on).  

- `million_as_numerals` determines whether magnitude words (thousands, millions, etc.) are in Kanji and the rest in Arabic, or numbers are entirely in Arabic. When million_as_numerals is True, magnitudes are written in Kanji, as shown below. This affects prices also, so $50.000 is written as $5万.  
  万 10.000  
  億 100.000.000  
  兆 1.000.000.000.000  
  京 10.000.000.000.000  

You can control how numbers are displayed by combining `Arabic_numerals_not_Kanji` and `million_as_numerals`: 

| All Kanji                           | Half-formatted (default)                             | All Arabic                           |
| ----------------------------------- | ---------------------------------------------------- | ------------------------------------ |
| `Arabic`: false                     | `Arabic`: true<br />`million`: true                  | `Arabic`: true<br />`million`: false |
| All numbers are displayed in Kanji. | Magnitude words are in Kanji and the rest in Arabic. | All numbers are displayed in Arabic. |
| 三                                  | 3                                                    | 3                                    |
| 十一                                | 11                                                   | 11                                   |
| 六十五                              | 65                                                   | 65                                   |
| 八百三十七                          | 837                                                  | 837                                  |
| 千                                  | 1,000                                                | 1,000                                |
| 千九百四十五                        | 1,945                                                | 1,945                                |
| 八千五百                            | 8,500                                                | 8,500                                |
| 一万                                | 1万                                                  | 10,000                               |
| 一万五千                            | 1万5,000                                             | 15,000                               |
| 一億三千万                          | 1億3,000万                                           | 130,000,000                          |
| 二億五                              | 2億5                                                 | 200,000,005                          |

### Type vs. Params

> Type vs. param

```plaintext
Utterance: "My address is seven twenty six brookline avenue cambridge mass"

With any formattingType and 
formattingParam format_addresses: true
--> My address is 726 Broookline Ave., Cambridge, MA

formattingParam format_addresses: false
--> My address is 726 Brookline Avenue Cambridge Mass
```

Some formattingTypes have similar names to formattingParams, for example formattingType date, phone, time, and address and the formattingParams format_dates, format_times, and so on. What's the difference? 

These formattingTypes tells Krypton how to *interpret ambiguous numbers*, while formattingParams tell Krypton how to *format text for display*. For example: 

- `formattingType":"date"`: Interpret "eleven twenty six" as the date 11/26 (November 26).   

- `"formattingParam":"format_dates": true`: Display numbers identified as dates in the locale's date format, for example, 11/26 in American English.  

- `formattingParam":"format_dates": false`: Display numbers as cardinal numbers (1126) or write them out (eleven twenty-six) even for numbers identified as dates.   

When you set formattingParams, be aware of the default for the formattingType to which it belongs. For example, format_prices is True for most formattingTypes, so there is no need to set it explicitly if you want prices to be shown with currency symbols and characters. 

Other types—all_as_words, num_as_digits, and all_as_katakana—set general instructions for displaying the transcription and are not related to interpretation of ambiguous numbers. 
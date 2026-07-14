/* ============================================================
   Parser.js — Port of Parser.kt
   Parses voice input into { toothNumber, values }
   ============================================================ */

const Parser = (() => {
  const numberWords = {
    'zero': 0, 'one': 1, 'two': 2, 'three': 3, 'four': 4,
    'five': 5, 'six': 6, 'seven': 7, 'eight': 8, 'nine': 9,
    'ten': 10, 'eleven': 11, 'twelve': 12, 'thirteen': 13,
    'fourteen': 14, 'fifteen': 15, 'sixteen': 16, 'seventeen': 17,
    'eighteen': 18, 'nineteen': 19, 'twenty': 20, 'thirty': 30,
    'forty': 40, 'fifty': 50
  };

  /**
   * Extracts tooth number and probing values from speech text.
   * Supports digits, word numbers (e.g. "eleven"), compound phrases, and non-numeric punctuation.
   * Example input: "Tooth eleven: three, four, five"
   * Result: { toothNumber: 11, values: [3, 4, 5] }
   */
  function parseSpeech(input) {
    try {
      console.log('[Parser] Original voice input:', input);

      // Clean up text: convert to lowercase and replace punctuation with spaces
      let cleaned = input.toLowerCase()
        .replace(/[-,:;._]/g, ' ')
        .replace(/tooth number/g, 'tooth')
        .replace(/tooth no/g, 'tooth');

      // Reconstruct string replacing word numbers with their digit strings
      const words = cleaned.split(/\s+/).filter(w => w.length > 0);
      const reconstructed = [];

      let i = 0;
      while (i < words.length) {
        const currentWord = words[i];
        if (numberWords.hasOwnProperty(currentWord)) {
          let valNum = numberWords[currentWord];

          // Support compound numbers like "twenty one"
          if (i + 1 < words.length && (currentWord === 'twenty' || currentWord === 'thirty')) {
            const nextWord = words[i + 1];
            if (numberWords.hasOwnProperty(nextWord) && numberWords[nextWord] < 10) {
              valNum += numberWords[nextWord];
              i++; // skip next word
            }
          }
          reconstructed.push(valNum.toString());
        } else {
          reconstructed.push(currentWord);
        }
        i++;
      }

      const processText = reconstructed.join(' ');
      console.log('[Parser] Cleaned speech text:', processText);

      // Attempt to match "tooth" followed by tooth number
      const regex = /(?:tooth)\s*(\d+)(.*)/i;
      const match = processText.match(regex);

      if (match) {
        const toothNumber = parseInt(match[1], 10);
        const restOfText = match[2];

        // Extract all numbers from the rest of the text
        const digitsMatches = restOfText.match(/\b\d+\b/g);
        const values = digitsMatches
          ? digitsMatches.map(v => parseInt(v, 10)).filter(v => v >= 0 && v <= 10)
          : [];

        if (toothNumber >= 1 && toothNumber <= 32 && values.length > 0) {
          return { toothNumber, values };
        }
        return null;
      } else {
        // Fallback: if "tooth" was not recognized, check for sequence of numbers.
        // First number = tooth, rest = probing depths.
        const allMatches = processText.match(/\b\d+\b/g);
        const allNumbers = allMatches ? allMatches.map(v => parseInt(v, 10)) : [];

        if (allNumbers.length >= 2) {
          const toothNumber = allNumbers[0];
          const values = allNumbers.slice(1).filter(v => v >= 0 && v <= 10);

          if (toothNumber >= 1 && toothNumber <= 32 && values.length > 0) {
            return { toothNumber, values };
          }
        }
        return null;
      }
    } catch (e) {
      console.error('[Parser] Error parsing speech:', e);
      return null;
    }
  }

  function classifyDisease(values) {
    if (!values || values.length === 0) return 'Unknown';
    const maxVal = Math.max(...values);
    if (maxVal <= 3) return 'Healthy';
    if (maxVal <= 5) return 'Gingivitis';
    return 'Periodontitis';
  }

  return { parseSpeech, classifyDisease };
})();

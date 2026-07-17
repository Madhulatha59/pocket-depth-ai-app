/* ============================================================
   voice.js — Speech Recognition wrapper using Web Speech API
   Web equivalent of Android's VoiceRecognizerManager
   ============================================================ */

var VoiceService = (() => {
  let recognition = null;
  let isListening = false;
  let callback = null;
  let errorCallback = null;

  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;

  if (SpeechRecognition) {
    recognition = new SpeechRecognition();
    recognition.continuous = true;
    recognition.interimResults = false;
    recognition.lang = 'en-US';

    recognition.onstart = () => {
      isListening = true;
      console.log('[VoiceService] Speech recognition started');
    };

    recognition.onresult = (event) => {
      const resultIndex = event.resultIndex;
      const transcript = event.results[resultIndex][0].transcript.trim();
      console.log('[VoiceService] Recognized transcript:', transcript);
      if (callback) {
        callback(transcript);
      }
    };

    recognition.onerror = (event) => {
      console.error('[VoiceService] Speech recognition error:', event.error);
      isListening = false;
      if (errorCallback) {
        errorCallback(event.error);
      }
    };

    recognition.onend = () => {
      isListening = false;
      console.log('[VoiceService] Speech recognition ended');
      // Auto restart if continuous listening is desired and we are supposed to be listening
      if (isListening) {
        try {
          recognition.start();
        } catch (e) {
          console.error('[VoiceService] Restart failed:', e);
        }
      }
    };
  } else {
    console.warn('[VoiceService] Web Speech API is not supported in this browser.');
  }

  return {
    isSupported() {
      return recognition !== null;
    },
    start(onResult, onError) {
      if (!recognition) {
        if (onError) onError('Speech API not supported');
        return;
      }
      callback = onResult;
      errorCallback = onError;
      try {
        isListening = true;
        recognition.start();
      } catch (e) {
        console.error('[VoiceService] Start failed:', e);
        if (onError) onError(e.message);
      }
    },
    stop() {
      if (!recognition) return;
      isListening = false;
      try {
        recognition.stop();
      } catch (e) {
        console.error('[VoiceService] Stop failed:', e);
      }
    }
  };
})();

window.VoiceService = VoiceService;


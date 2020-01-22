(ns alita.core
  (:import java.awt.Robot
           (java.awt.event InputEvent KeyEvent)
           (edu.cmu.sphinx.api Configuration SpeechResult LiveSpeechRecognizer))
  (:gen-class))

(def ^:dynamic *robot* (Robot.))
(def ^:dynamic *speech-config* (Configuration.))
(.setAcousticModelPath *speech-config* "resource:/edu/cmu/sphinx/models/en-us/en-us")
(.setDictionaryPath *speech-config* "resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict")
(.setLanguageModelPath *speech-config* "resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin")

(def ^:dynamic *recognizer* (LiveSpeechRecognizer. *speech-config*))

(defn get-result
  []
  (let [res (.getResult *recognizer*)]
    (if (some? res)
      (println (.getHypothesis res)))))

(defn hold-keycode
  [keycode]
  (.keyPress *robot* keycode))

(defn release-keycode
  [keycode]
  (.keyRelease *robot* keycode))

(defn poke-keycode
  [keycode]
  (hold-keycode keycode)
  (release-keycode keycode))

(defn poke
  [s]
  (let [[_ modifier-key key] (re-find #"(?:([CMS])-)?([a-z0-9])" s)
        keycode (KeyEvent/getExtendedKeyCodeForChar (int (first key)))
        modifier (get {"C" KeyEvent/VK_CONTROL
                       "M" KeyEvent/VK_ALT
                       "S" KeyEvent/VK_SHIFT} modifier-key)]
    (if (some? modifier)
      (hold-keycode modifier))
    (poke-keycode keycode)
    (if (some? modifier)
      (release-keycode modifier))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

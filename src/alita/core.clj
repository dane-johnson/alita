(ns alita.core
  (:require [clojure.java.io :refer [resource input-stream make-parents reader writer file]])
  (:import java.awt.Robot
           (java.awt.event InputEvent KeyEvent)
           (net.harawata.appdirs AppDirs AppDirsFactory)
           (edu.cmu.sphinx.api Configuration SpeechResult
                               StreamSpeechRecognizer
                               LiveSpeechRecognizer))
  (:gen-class))

(def ^:dynamic *null* (writer "/dev/null"))

(defmacro shush
  [form]
  `(with-bindings {#'*out* *null*} ~form))

;; Wrap the stupid appdirs factory thing
(defn config-dir
  []
  (-> (AppDirsFactory/getInstance)
      (.getUserConfigDir "alita" nil nil)))

(defn make-default-config
  []
  (let [filename (str (config-dir) "/config.clj")]
    (make-parents filename)
    (spit filename (slurp (resource "alita/configs/default.clj")))))

(defn config-file
  []
  (let [config (file (str (config-dir) "/config.clj"))]
    (if (not (.exists config))
      (do (make-default-config) (recur))
      config)))

(defn load-config
  []
  (let [file (config-file)]
    (if (some? file)
      (read (java.io.PushbackReader. (reader file)))
      (do
        (make-default-config)
        (recur)))))

(def ^:dynamic *robot* (Robot.))
(def ^:dynamic *speech-config* (Configuration.))
(doto *speech-config*
  (.setAcousticModelPath "resource:/edu/cmu/sphinx/models/en-us/en-us")
  (.setDictionaryPath "resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict")
  (.setLanguageModelPath "resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin")
  (.setSampleRate 8000)
  (.setGrammarPath (.getFile (resource "alita/grammars")))
  (.setUseGrammar true)
  (.setGrammarName "alita"))

(def ^:dynamic *recognizer* (StreamSpeechRecognizer. *speech-config*))

(defn get-result
  []
  (let [res (shush (.getResult *recognizer*))]
    res))

(defn parse-response
  [res]
  (if res (.getHypothesis res)))

(defn response->keybinding
  [res mapping]
  (if res
    (some #(if (clojure.string/ends-with? res (key %)) (val %)) mapping)))

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
  [& args]
  (let [keymapping (load-config)]
    (shush (.startRecognition *recognizer* (input-stream (first args))))
    (while true
      (let [res (parse-response (get-result))
            keybinding (response->keybinding res keymapping)]
        (if keybinding
          (poke keybinding))))))

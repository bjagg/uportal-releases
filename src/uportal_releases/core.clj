(ns uportal-releases.core
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]
            [clojure.set :refer :all]
            [environ.core :refer [env]])
  (:gen-class))

(def orgs ["Jasig" "uPortal-project" "uPortal-contrib"])
(def token (env :github-token))
(def token-str (str "token " (env :github-token)))

(defn get-repos [org]
  (loop [url (str "https://api.github.com/orgs/" org "/repos")
         repos []]
    (if-not url
      repos
      (let [response (client/get url {:headers {"Accept" "application/vnd.github.inertia-preview+json" "authorization" token-str}})
            json (:body response)
            resp-repos (map #(get % :name) (parse-string json true))]
        (recur (get-in response [:links :next :href])
               (concat repos resp-repos))))))

(defn get-tags
  "For the given array of GitHub project and repo, return all tags"
  [[proj repo]]
  (loop [url (str "https://api.github.com/repos/" proj "/" repo "/tags")
         tags []]
    (if-not url
      tags
      (let [response (client/get url {:accept :json :headers {:authorization token-str}})
            json (:body response)
            resp-tags (map #(get % :name) (parse-string json true))]
        (recur (get-in response [:links :next :href])
               (concat tags resp-tags))))))

(def release-tag #"\d+[\.-]\d+[\.-]\d+")
(def major-tag #"\d+[\.-]0[\.-]0")
(def minor-tag #"\d+[\.-][1-9]+\d*[\.-]0")
(def patch-tag #"\d+[\.-]\d+[\.-][1-9]+")

(def ignore-tags [#"^ssp" #"^umobile" #"RC" #"M\d" #"Milestone" #"patches"])

(defn regex-pred
  "Create a predicate function from regex"
  [regex]
  (fn [tag] (re-find regex tag)))

(defn build-ignore-filter
  "Create a some-fn of predicate functions base of ingore-tags regex"
  [patterns]
  (apply some-fn (map regex-pred patterns)))

(defn filter-some-tags
  "Filter out milestone and release candidate tags"
  [tags]
  (filter (build-ignore-filter ignore-tags) tags))

(defn print-releases [[ org repo]]
  (let [up-tags (into #{} (filter #(re-find release-tag %) (get-tags [org repo])))
        ignored-tags (into #{} (filter-some-tags up-tags))
        rel-tags (difference up-tags ignored-tags)
        major-tags (filter #(re-find major-tag %) rel-tags)
        minor-tags (filter #(re-find minor-tag %) rel-tags)
        patch-tags (filter #(re-find patch-tag %) rel-tags)]

    (print (str org "/" repo " --"))
    (print " Major:" (count major-tags))
    (print ", Minor:" (count minor-tags))
    (print ", Patch:" (count patch-tags))
    (println ", Total:" (count rel-tags))))

(defn get-org-repos
  "Get repos for an org and return in a collection of (org repo1) entries"
  [org]
    (map (juxt (constantly org) identity) (get-repos org)))

(defn -main [& args]
  (println token)
  (println token-str)
  (let [org-repos (mapcat get-org-repos orgs)]
    (doall (map print-releases org-repos))))

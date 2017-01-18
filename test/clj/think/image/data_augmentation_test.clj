(ns think.image.data-augmentation-test
  (:require [think.image.data-augmentation :refer [rect-mog->affinetransform 
                                                   random-rect-mog 
                                                   xform-point
                                                   random]]
            [clojure.pprint :as pp]
            [clojure.test :refer :all]
            ))

(defn- corner-points [w h]
  [[0 0] [w 0] [w h] [0 h]])

; pretty bad standard of equality!  but points just need to be equal on a pixel level. 
(defn- dbleq [a b]
  (< (Math/abs (- a b)) 0.1))

(defn- within-rect? [x1 y1 x2 y2 x y]
  (let [x (Math/round x)
        y (Math/round y)]
    (and (<= x1 x)
         (<= x x2)
         (<= y1 y)
         (<= y y2))))

(defn test-mog-pts [m]
  (let [tx (rect-mog->affinetransform m)
        itx (.createInverse tx)
        [sw sh] (:source-dims m)
        tpoints (apply corner-points (:target-dims m))
        rcorners (map (partial xform-point tx) (:corners m))
        invpoints (map (partial xform-point itx) tpoints)]
    (is (every? identity (map (fn [[x y]] (within-rect? 0 0 sw sh x y))
                              (:corners m)))
        (str "corner points not contained in source image. source dims: " [sw sh] " points: " (pr-str (:corners m))))
    ; check xforming the target image dims to the corner dims.
    (is (every? identity (map dbleq (apply concat tpoints) (apply concat rcorners)))
        (str "failure 'A' comparing points: " tpoints " and " (pr-str rcorners)))
    ; also the corner points back to the target dims. 
    (is (every? identity (map dbleq (apply concat (:corners m)) (apply concat invpoints)))
        (str "failure 'B' comparing points: " (:corners m) " and " (pr-str invpoints)))))

(defn test-many-mogs [n]
  (let [mogs (map (fn [_] (random-rect-mog [(Math/floor (random 10 4000)) (Math/floor (random 10 4000))]
                                            [(Math/floor (random 10 4000)) (Math/floor (random 10 4000))]
                                            true
                                            true
                                            (random 0.1 100.0)
                                            1.0))
                   (repeat n 0))]
    (doall (map test-mog-pts mogs))))

(deftest rect-mog-sanity-check []
  (test-many-mogs 1000))



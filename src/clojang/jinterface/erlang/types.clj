(ns clojang.jinterface.erlang.types
  (:require [clojang.jinterface.erlang :as erlang]
            [clojang.util :as util])
  (:refer-clojure :exclude [atom boolean char list float long map ref short]))

;;; >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
;;; Data types constructors
;;; >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

(defn atom
  "Constructor for an Erlang atom data type."
  [arg]
  (erlang/init 'atom arg))

(defn boolean
  "Constructor for an Erlang boolean (atom) data type."
  [bool]
  (erlang/init 'boolean bool))

(defn char
  "Constructor for an Erlang char."
  [ch]
  (erlang/init 'char ch))

(defn tuple
  "Provides a Java representation of Erlang tuples. Tuples are created from
  one or more arbitrary Erlang terms.

  The arity of the tuple is the number of elements it contains. Elements are
  indexed from 0 to (arity-1) and can be retrieved individually by using the
  appropriate index."
  [args]
  (erlang/init 'tuple args))

(defn list
  "Provides a Java representation of Erlang lists. Lists are created from
  zero or more arbitrary Erlang terms.

  The arity of the list is the number of elements it contains."
  ([]
    (erlang/init 'list))
  ([args]
    (erlang/init 'list args)))

(defn string
  "Provides a Java representation of Erlang strings."
  [str]
  (erlang/init 'string str))

(defn map
  "Provides a Java representation of Erlang maps. Maps are created from one
  or more arbitrary Erlang terms.

  The arity of the map is the number of elements it contains. The keys and
  values can be retrieved as arrays and the value for a key can be
  queried."
  [ks vs]
  (erlang/init 'map ks vs))

;;; >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
;;; Error handling
;;; >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

(util/add-err-handler #'erlang/init
  [java.lang.IllegalArgumentException,
   java.lang.InstantiationException]
  "[ERROR] could not instantiate object!")

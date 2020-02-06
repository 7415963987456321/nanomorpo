;;; This test should return nothing since comments are ignore by the lexer
;;; This is a single-line comment in morpho.

{;;;
    This is a multi-line comment in morpho.
;;;}

{;;;
    {;;;
        {;;;
            Nested comments;
        ;;;}
        ;;; Single-line comment inside nesting
    ;;;}
;;;}

{;;; This is a multi-line comment in morpho on one line  ;;;}

;;;More comments
;; bad comment

;;; Many
;;; one-line
;;; comments

;;;
; segédfüggvények
; mind megfelel a korlátos szélesség/mélység feltevésnek
; futás közben új lambda csak a let helyettesítésére van
; az unzip/zip párok definiálják az alapvető adatstruktúrákat
;;;

(define array-add
	(lambda (array digit element)
		(array-set
			array
			digit
			(cons
				element
				(array-ref array digit)))))

(define unzip-tree
	(lambda (tree base cont)
		(if (null? tree)
			(cont
				(make-array (- base 1))
				0
				#f
				'())
			(cont
				(car tree)
				(cadr tree)
				(caddr tree)
				(cadddr tree)))))

(define zip-tree
	(lambda (array digit parent)
		(list
			array
			digit
			(or
				(not (= 0 digit))
				(and
					(not (null? parent))
					(caddr parent)))
			parent)))

(define make-tree
	(lambda (base)
		(zip-tree (make-array (- base 1)) 1 '())))

(define tree->number
	(lambda (base tree)
		(unzip-tree tree base
			(lambda (array digit has-more-digits parent)
				(if has-more-digits
					(+ digit (* base (tree->number base parent)))
					digit)))))

(define unzip-prime
	(lambda (prime cont)
		(if (null? prime)
			(cont 0 '())
			(cont (car prime) (cdr prime)))))

(define unzip-lazy-down
	(lambda (lazy-down cont)
		(cont
			(car lazy-down)
			(cadr lazy-down))))

(define zip-lazy-down
	(lambda (position prime)
		(list position prime)))

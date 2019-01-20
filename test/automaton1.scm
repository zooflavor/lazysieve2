;;;
; ezek az automatába vannak építve
;;;

(define add
	(lambda (aa bb cc base cont)
		(let ((sum (+ aa bb cc)))
			(if (> base sum)
				(cont 0 sum)
				(cont 1 (- sum base))))))

(define array-ref
	(lambda (array index)
		(if (> index 1)
			(array-ref (cdr array) (- index 1))
			(car array))))

(define array-set
	(lambda (array index value)
		(if (> index 1)
			(cons
				(car array)
				(array-set (cdr array) (- index 1) value))
			(cons value (cdr array)))))

(define make-array
	(lambda (size)
		(if (> size 0)
			(cons '() (make-array (- size 1)))
			'())))

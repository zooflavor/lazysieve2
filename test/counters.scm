;;;
; a műveletek számlálása
;;;

(define counter-c 0)
(define counter-i 0)
(define counter-i- 0)
(define counter-r 0)
(define counter-r+- 0)
(define digits-cache (make-hasheqv))

(define init-counters
	(lambda (base)
		(set! counter-c 0)
		(set! counter-i 0)
		(set! counter-i- 0)
		(set! counter-r 0)
		(set! counter-r+- 0)
		(set! digits-cache (make-hasheqv))
		(define digits
			(lambda (number result)
				(if (= 0 number)
					result
					(digits
						(quotient number base)
						(+ result 1)))))
		(do ((pp primes (cdr pp)))
				((null? pp))
			(hash-set! digits-cache (car pp) (digits (car pp) 0)))))

(define inc-counter-c (lambda () (inc! counter-c)))
(define inc-counter-i (lambda () (inc! counter-i)))
(define inc-counter-i- (lambda () (inc! counter-i-)))
(define inc-counter-r (lambda () (inc! counter-r)))
(define inc-counter-r+- (lambda () (inc! counter-r+-)))

(define prime-digits
	(lambda (number)
		(hash-ref digits-cache number)))

(define check-counters
	(lambda (base nn print?)
		(define counter-all
			(+ counter-c counter-i counter-i- counter-r counter-r+-))
		(define all-min 0)
		(define all-max 0)
		(define 1/b-1 (/ 1 (- base 1)))
		
		(define prime-count 0)
		(define prime-digits-sum 0)
		(define prime-multiples 0)
		(define prime-multiples-digits+1/b-1+0 0)
		(define prime-multiples-digits-1+0 0)
		(define prime-multiples-digits-1-1 0)
		(for-each-prime nn
			(lambda (pp)
				(let ((pd (prime-digits pp))
						(qq (quotient nn pp)))
					(add! prime-count 1)
					(add! prime-digits-sum pd)
					(add! prime-multiples qq)
					(add! prime-multiples-digits+1/b-1+0
						(* qq (+ pd 1/b-1)))
					(add! prime-multiples-digits-1+0
						(* qq (- pd 1)))
					(add! prime-multiples-digits-1-1
						(* (- qq 1) (- pd 1))))))
		
		(define increments
			(let ir ((bi base) (kk nn) (rr 0))
				(if (= 0 kk)
					rr
					(ir
						(* base bi)
						(quotient kk base)
						(+ rr
							(* (modulo kk base)
								(- bi 1)
								1/b-1))))))
		
		(define check
			(lambda (symbol value min max)
				(if (not min)
					(set! min max)
					#f)
				(add! all-max max)
				(add! all-min min)
				(if (and #f (or (> min value) (< max value)))
					(begin
						(display "*** ")
						(display symbol)
						(display " <> ")
						(display min)
						(display " <> ")
						(display value)
						(display " <> ")
						(display max)
						(newline))
					#f)))
		
		(check 'c counter-c #f (* 2 (+ prime-count prime-digits-sum)))
		(check 'i counter-i #f (* 2 (- increments 1)))
		(check 'i- counter-i-
			prime-multiples-digits-1-1
			prime-multiples-digits+1/b-1+0)
		(check 'r counter-r #f (+ nn -1 (* 3 prime-multiples)))
		(check 'r+- counter-r+-
			(* 2 prime-multiples-digits-1+0)
			(* 2 prime-multiples-digits+1/b-1+0))
		(if print?
			(begin
				(display "nn ")
				(display nn)
				(display " %all-min/max-min ")
				(display (round (/ (* 100 (- counter-all all-min)) (- all-max all-min))))
				(display " %max-min/all ")
				(display (round (/ (* 100 (- all-max all-min)) counter-all)))
				(display " min ")
				(display all-min)
				(display " all ")
				(display counter-all)
				(display " max ")
				(display all-max)
				(newline)
				#f)
			#f)))

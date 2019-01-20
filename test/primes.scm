;;;
; prímek listája n-ig
;;;

(define primes '())

(define init-primes
	(lambda (nn)
		(define start-primes (current-milliseconds))
		(define small-primes '())
		
		(set! primes '())
		
		(define prime?
			(lambda (nn primes)
				(if (null? primes)
					#t
					(let ((prime (car primes)))
						(if (< nn (* prime prime))
							#t
							(if (= 0 (modulo nn prime))
								#f
								(prime? nn (cdr primes))))))))
		
		(do ((ii 2 (+ ii 1)))
				((< nn (* ii ii)))
			(if
				(let prime? ((pp small-primes))
					(or (null? pp)
						(and (not (= 0 (modulo ii (car pp))))
							(prime? (cdr pp)))))
				(set! small-primes (cons ii small-primes))
				#f))
		
		(set! primes small-primes)
		(set! small-primes (reverse small-primes))
		
		(do ((ii (car primes) (+ ii 1)))
				((< nn ii))
			(if
				(let prime? ((pp small-primes))
					(if (null? pp)
						#t
						(let ((p1 (car pp)))
							(if (< nn (* p1 p1))
								#t
								(if (= 0 (modulo ii p1))
									#f
									(prime? (cdr pp)))))))
				(set! primes (cons ii primes))
				#f))
		
		(set! primes (reverse primes))
		
		(define end-primes (current-milliseconds))
		(display "primes ms: ")
		(display (- end-primes start-primes))
		(newline)))

(define for-each-prime
	(lambda (nn func)
		(let fepr ((pp primes))
			(if (null? pp)
				#f
				(let ((p1 (car pp)))
					(if (< nn p1)
						#f
						(begin
							(func p1)
							(fepr
								(cdr pp)))))))))

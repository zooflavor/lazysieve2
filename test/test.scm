(#%require scheme)

;;;
; teszt és mérés és mérésteszt
;;;

(require data/heap)

(define-syntax add!
	(syntax-rules ()
		((_ vv xx) (set! vv (+ vv xx)))))

(define-syntax inc!
	(syntax-rules ()
		((_ vv) (set! vv (+ vv 1)))))

(load "automaton1.scm")
(load "automaton2.scm")
(load "automaton3.scm")
(load "counters.scm")
(load "primes.scm")

(define nn 10000)

(init-primes nn)

(do ((base 2 (+ base 1)))
		((< 10 base))
	(let ((start-base (current-milliseconds))
			(end-base 0)
			(time-check-counters 0)
			(time-increment 0)
			(time-prime? 0))
		(display "base: ")
		(display base)
		(newline)
		
		(init-counters base)
		
		(do ((ii 2 (+ ii 1))
					(i-prime? #f)
					(tree (make-tree base))
					(primes2 primes)
					(time0 0)
					(time1 0)
					(time2 0)
					(time3 0))
				((< nn ii))
			(set! time0 (current-milliseconds))
			(set! i-prime?
				(let ipr ()
					(if (null? primes2)
						#f
						(let ((pp (car primes2)))
							(cond
								((> ii pp)
									(set! primes2 (cdr primes2))
									(ipr))
								((= ii pp) #t)
								(#t #f))))))
			(set! time1 (current-milliseconds))
			(increment-up tree '() base
				(lambda (prime? new-tree)
					(set! tree new-tree)
					(let ((ii (tree->number base tree)))
						(if (equal? prime? i-prime?)
							#f
							(begin
								(display "error - ii:")
								(display ii)
								(display " - prime? ")
								(display prime?)
								(display " - i-prime? ")
								(display i-prime?)
								(newline)
								(error "prime? error")))
						(set! time2 (current-milliseconds))
						(check-counters base ii (= ii nn)))))
			(set! time3 (current-milliseconds))
			(add! time-prime? (- time1 time0))
			(add! time-increment (- time2 time1))
			(add! time-check-counters (- time3 time2)))
		
		(set! end-base (current-milliseconds))
		(display "base ")
		(display base)
		(display " time ms: ")
		(display (- end-base start-base))
		(display " prime? ")
		(display time-prime?)
		(display " increment ")
		(display time-increment)
		(display " check-counters ")
		(display time-check-counters)
		(newline)))

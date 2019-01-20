(#%require scheme)

(define dd
	(lambda (base xx yy)
		(let dr ((x2 xx) (y2 yy) (hh 0))
			(let ((x3 (quotient x2 base)) (y3 (quotient y2 base)))
				(if (= x3 y3)
					(+ (* (- base 1) hh) (- y2 (* y3 base)) -1)
					(dr x3 y3 (+ hh 1)))))))

(define all-cache #f)
(define base #f)
(define buckets #f)
(define free-cache #f)
(define max-cache #f)
(define sieve-position #f)
(define slow-counter #f)
(define used-cache #f)

(define init-sieve
	(lambda (base2 cache limit)
		(let ((size (+ (dd base2 0 (* 2 limit)) 1)))
			(set! all-cache cache)
			(set! base base2)
			(set! buckets (make-vector size '()))
			(set! free-cache cache)
			(set! max-cache (- size 1))
			(set! sieve-position 1)
			(set! slow-counter 0)
			(set! used-cache (make-vector size 0)))))

(define count-reads
	(lambda (bucket index)
		(let ((used (vector-ref used-cache index)))
			(set! free-cache (+ free-cache used))
			(set! slow-counter (+ slow-counter (length bucket) (- used)))
			(vector-set! used-cache index 0))))

(define increment-used-cache
	(lambda (index)
		(vector-set! used-cache index
			(+ (vector-ref used-cache index) 1))
		(if (> index max-cache)
			(set! max-cache index)
			#f)))

(define count-write
	(lambda (index)
		(if (< 0 free-cache)
			(begin
				(set! free-cache (- free-cache 1))
				(increment-used-cache index))
			(begin
				(set! slow-counter (+ slow-counter 1))
				(let cwr ((index2 max-cache))
					(if (>= index index2)
						#f
						(let ((used (vector-ref used-cache index2)))
							(if (>= 0 used)
								(begin
									(set! max-cache (- index2 1))
									(cwr (- index2 1)))
								(begin
									(vector-set! used-cache index2
										(- used 1))
									(increment-used-cache index))))))))))

(define sieve
	(lambda ()
		(let* ((p1 (+ sieve-position +1))
				(d2 (dd base sieve-position p1)))
			(set! sieve-position p1)
			(let* ((bucket (vector-ref buckets d2))
					(prime? (null? bucket)))
				(vector-set! buckets d2 '())
				(count-reads bucket d2)
				(if prime?
					(set! bucket
						(list (cons sieve-position sieve-position)))
					#f)
				(let br ((bucket2 bucket))
					(if (null? bucket2)
						prime?
						(let ((position (caar bucket2))
								(prime (cdar bucket2)))
							(if (= position sieve-position)
								(set! position (+ position prime))
								#f)
							(let ((d3 (dd base sieve-position position)))
								(count-write d3)
								(vector-set! buckets d3
									(cons (cons position prime)
										(vector-ref buckets d3)))
								(br (cdr bucket2))))))))))

(define measure
	(lambda (base cache limit steps)
		(init-sieve base cache limit)
		(do ()
				((<= limit sieve-position))
			(sieve)
			(if (= 0 (modulo sieve-position steps))
				(begin
					(display "(")
					(display (quotient sieve-position steps))
					(display ",")
					(display (quotient slow-counter 1000))
					(display ")"))
				#f))
		(newline)))

(measure 2 0 1000000 100000)
(measure 2 1 1000000 100000)
(measure 2 2 1000000 100000)
(measure 2 3 1000000 100000)
(measure 2 4 1000000 100000)

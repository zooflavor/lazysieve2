;;;
; a megszámolt formulaátírások
; a formula egy fv.hívás
; az átírás egy új fv.hívás tail position-ben
;;;

(define copy-down
	(lambda (tree base prime prime-reverse cont)
		(inc-counter-c)
		(if (null? prime-reverse)
			(readd
				base
				cont
				(list (zip-lazy-down '() prime))
				#t
				tree)
			(copy-down
				tree
				base
				(cons (car prime-reverse) prime)
				(cdr prime-reverse)
				cont))))

(define copy-up
	(lambda (tree tree-digits base prime-reverse cont)
		(inc-counter-c)
		(unzip-tree tree-digits base
			(lambda (array digit has-more-digits parent)
				(if has-more-digits
					(copy-up
						tree
						parent
						base
						(cons digit prime-reverse)
						cont)
					(copy-down
						tree
						base
						'()
						prime-reverse
						cont))))))

(define increment-down
	(lambda (tree tree-reverse base lazy-down lazy-down-reverse cont)
		(if (null? tree-reverse)
			(begin
				(inc-counter-i)
				(if (null? lazy-down)
					(copy-up tree tree base '() cont)
					(readd base cont lazy-down #f tree)))
			(if (null? lazy-down)
				(begin
					(inc-counter-i)
					(unzip-tree tree-reverse base
						(lambda (array digit has-more-digits parent)
							(increment-down
								(zip-tree
									array
									digit
									tree)
								parent
								base
								lazy-down-reverse
								'()
								cont))))
				(unzip-lazy-down (car lazy-down)
					(lambda (position prime)
						(inc-counter-i-)
						(if (= 0 (car position))
							(increment-down
								tree
								tree-reverse
								base
								(cdr lazy-down)
								(cons
									(zip-lazy-down
										(cdr position)
										prime)
									lazy-down-reverse)
								cont)
							(unzip-tree tree-reverse base
								(lambda (array digit has-more-digits parent)
									(increment-down
										tree
										(zip-tree
											(array-add
												array
												(car position)
												(zip-lazy-down
													(cdr position)
													prime))
											digit
											parent)
										base
										(cdr lazy-down)
										lazy-down-reverse
										cont))))))))))

(define increment-up
	(lambda (tree tree-reverse base cont)
		(inc-counter-i)
		(unzip-tree tree base
			(lambda (array digit has-more-digits parent)
				(add digit 0 1 base
					(lambda (carry new-digit)
						(if (= 0 carry)
							(increment-down
								(zip-tree
									(array-set
										array
										new-digit
										'())
									new-digit
									parent)
								tree-reverse
								base
								(array-ref array new-digit)
								'()
								cont)
							(increment-up
								parent
								(zip-tree
									array
									new-digit
									tree-reverse)
								base
								cont))))))))

(define readd
	(lambda (base cont lazy-downs prime? tree)
		(inc-counter-r)
		(if (null? lazy-downs)
			(cont prime? tree)
			(unzip-lazy-down (car lazy-downs)
				(lambda (position prime)
					(readd-up
						base
						0
						cont
						(cdr lazy-downs)
						'()
						prime
						prime?
						prime
						tree
						'()))))))

(define readd-down
	(lambda (base cont lazy-downs prime? tree tree-reverse)
		(if (null? tree-reverse)
			(begin
				(inc-counter-r)
				(readd base cont lazy-downs prime? tree))
			(begin
				(inc-counter-r+-)
				(unzip-tree tree-reverse base
					(lambda (array digit has-more-digits parent)
						(readd-down
							base
							cont
							lazy-downs
							prime?
							(zip-tree
								array
								digit
								tree)
							parent)))))))

(define readd-up
	(lambda (base carry cont lazy-downs position prime prime? prime-remaining tree tree-reverse)
		(unzip-prime prime-remaining
			(lambda (prime-digit prime-parent)
				(unzip-tree tree base
					(lambda (array tree-digit has-more-digits tree-parent)
						(add tree-digit prime-digit carry base
							(lambda (new-carry new-digit)
								(if (and (= 0 new-carry)
										(null? prime-parent))
									(begin
										(inc-counter-r)
										(readd-down
											base
											cont
											lazy-downs
											prime?
											(zip-tree
												(array-add
													array
													new-digit
													(zip-lazy-down
														position
														prime))
												tree-digit
												tree-parent)
											tree-reverse))
									(begin
										(inc-counter-r+-)
										(readd-up
											base
											new-carry
											cont
											lazy-downs
											(cons new-digit position)
											prime
											prime?
											prime-parent
											tree-parent
											(zip-tree
												array
												tree-digit
												tree-reverse))))))))))))

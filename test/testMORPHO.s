;;; Morpho prufa úr morho handbók
"reverse.mmod" =
    {{reverse 
    = fun(x) {
        var y=[];
            while( x )
            {
                y = head(x) : y;
                x = tail(x);
            };
        y;
    };
}};

show "reverse.mmod";

;;; Another morpho example from the morpho.cs.hi.is website:
"map.mexe" = main in
!
{{
    map = fun(f,lst)
    {
            lst==null && (return []);
                f(head(lst)) : map(f,tail(lst));
    };

    main = fun()
    {
            var lst = [1,2,3,4,5];
                var f = fun(x) { x*x; };

                    writeln(map(f,lst));
    };
}}
*
BASIS
;


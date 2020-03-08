main () {
    writeln("Fibonacci af 7:");
    writeln(fibo(7));
}

fibo(n){
    if(n <= 2){
        return 1;
    } else {
        return fibo(n-1) + fibo(n-2);
    };
        writeln(n);
}



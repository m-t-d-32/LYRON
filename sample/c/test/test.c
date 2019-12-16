void checkBaseCals(){
    int a = 3, b;
    b = 445;
    print(a + b * 3);
    print(b / 3 + a);
    print(b - a);
    //Pow
    print(a ^ 5);
}
void checkRelationCals(){
    int b = 4;
    print(b > 0);
    int c = -3;
    print(b < c);
    int d = b <= (c + 7);
    print(d);
}
void checkCopyCals(){
    typedef int A;
    A b = 3;
    typedef int B;
    B a = b;
    print(a);
}
void checkLogicalCals(){
    print(3 && 0);
    print(0 || 3);
}
void checkIfCondition(){
    int a = 4;
    if (a == 4 && a + 4 != 8){
        print(4);
    }
    else if (a == 5){
        print(5);
    }
    else {
        print(6);
    }
}
void checkForCondition(){
    int i;
    for (i = 0; i < 10; i =i + 1){
        if (i == 6){
            continue;
        }
        print(i + 1);
    }
}
void checkWhileCondition(){
    int j = 10;
    int k;
    while (j > 0){
        input(k);
        j = j - 11;
        print(j);
    }
}
int checkStructs(){
    typedef struct {
        int a[10];
    }A;
    A b;
    b.a[5] = 6;
    b.a[6] = b.a[5] + 6;
    print(b.a[5]);
    print(b.a[6]);
    return 66;
}
A checkReturnStruct(){
    A a;
    a.a[0] = 0;
    input(a.a[1]);
    return a;
}
int fact(int n){
    int temp;
    if (n == 1){
        return n;
    }
    else {
        temp = n * fact(n - 1);
        return temp;
    }
}
int main(){
    typedef struct {
        int a;
        A b;
    }B;
    B b;
    b.b.a[2] = 5;
    B c = b;
    print(c.b.a[2]);
    checkBaseCals();
    checkCopyCals();
    checkForCondition();
    checkIfCondition();
    checkLogicalCals();
    checkRelationCals();
    checkWhileCondition();
    print(checkStructs());
    A a = checkReturnStruct();
    print(a.a[0]);
    print(a.a[1]);
    print(fact(10));
    return 0;
}
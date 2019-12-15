void main(){
    typedef struct {
        int c;
    }C;
    typedef struct{
        C b[6][6];
    }B;
    B a[5];
    a[3].b[5][2].c = 9999;
    print(a[3].b[5][2].c);
    print(a[3].b[4][2].c);
    input(a[3].b[4][2].c);
    print(a[3].b[4][2].c);
}
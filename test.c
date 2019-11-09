int main()
{
    int a,b,c;
    int maxs=max(a,max(b,c));
    printf(maxs);
}
int max(int x,int y)
{
    int t;
    if (x > y){
    t=x;}
    else{
    t=y;}
    return t;
}
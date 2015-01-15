public class Temp
{
    public void act(String s)
    {
        if(s.matches("[A-Z][a-z]*"))
            System.out.println("首字母大写其余小写 " + s);
        else if(s.matches("[A-Z][a-z]*[-]*[A-Z][a-z]*"))
            System.out.println("连字号名字 " + s);
        else if(s.matches("[A-Z]*"))
            System.out.println("全大写 " + s);
        else if(s.contains("&"))
            System.out.println("含有& " + s);
        else
            System.out.println("_RARE_" + s);
    }

    public static void main(String[] args)
    {
        Temp t = new Temp();
        t.act("Qingxiang");
        t.act("Jia");
        t.act("Lee");
        t.act("Qingxiang-Jia");
        t.act("Marc-Darthy");
        t.act("ENGLAND");
        t.act("SINGAPORE");
        t.act("R&D");
        t.act("&");
        t.act("JIA&ZHU");
        t.act("U>S>");
        t.act("dsfdsf");
    }
}

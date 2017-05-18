#coding:utf-8
#,"2 192.168.2.2"
def dictRoad(list,ip,dict):
    if dict.has_key(list):
        dict[list].append(ip)
    else:
        dict[list]=[]
        dict[list].append(ip)

if __name__=="__main__":
    list1 = ("1 192.168.1.2")
    list2 = ("1 192.168.1.2")
    list3 = ("1 192.168.1.3")
    dict = {}
    dictRoad(list1,'1',dict)
    dictRoad(list2,'2',dict)
    dictRoad(list3,'3',dict)
    print dict
"""Geometry recovered from the original Small/Middle/Large JSP loops.
Current fictional events use stable A01...AD30 identifiers, not old numeric IDs.
"""
VENUES={1:('小型',10,10,None),2:('中型',20,20,(7,14,7,14)),3:('大型',30,30,(11,21,10,21))}
ACTIVITY_VENUES={1:1,2:2,3:3,4:1,6:1}
def row_label(row):
 text=''
 while row:row,n=divmod(row-1,26);text=chr(65+n)+text
 return text
def seats(scope):
 _,rows,cols,gap=VENUES[scope]
 return [f'{row_label(r)}{c:02d}' for r in range(1,rows+1) for c in range(1,cols+1) if not(gap and gap[0]<=r<=gap[1] and gap[2]<=c<=gap[3])]

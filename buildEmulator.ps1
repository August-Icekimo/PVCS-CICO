# 改變change.dat內容
$out = new-object byte[] 4096; 
(new-object Random).NextBytes($out); [IO.File]::WriteAllBytes('change.dat', $out)
# 新增檔案
$out = new-object byte[] 4096; 
$names = -join ((65..90) + (97..122) | Get-Random -Count 5 | ForEach-Object {[char]$_})
(new-object Random).NextBytes($out); [IO.File]::WriteAllBytes("Add-$names", $out)
# 增加test子附錄下檔案
$out = new-object byte[] 4096; 
$names = -join ((65..90) + (97..122) | Get-Random -Count 5 | ForEach-Object {[char]$_})
(new-object Random).NextBytes($out); [IO.File]::WriteAllBytes("test\Add-$names", $out)
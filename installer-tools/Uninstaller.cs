using System;
using System.Diagnostics;
using Microsoft.Win32;
using System.Windows.Forms;

class Uninstaller
{
    [STAThread]
    static void Main()
    {
        const string appName = "Rellenador EX-17";

        string[] registryPaths =
        {
            @"SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall",
            @"SOFTWARE\WOW6432Node\Microsoft\Windows\CurrentVersion\Uninstall"
        };

        foreach (string path in registryPaths)
        {
            using (RegistryKey root = Registry.LocalMachine.OpenSubKey(path))
            {
                if (root == null)
                    continue;

                foreach (string subKeyName in root.GetSubKeyNames())
                {
                    using (RegistryKey subKey = root.OpenSubKey(subKeyName))
                    {
                        if (subKey == null)
                            continue;

                        string displayName = subKey.GetValue("DisplayName") as string;

                        if (!string.Equals(displayName, appName,
                                StringComparison.OrdinalIgnoreCase))
                            continue;

                        string uninstallString =
                            subKey.GetValue("UninstallString") as string;

                        if (string.IsNullOrWhiteSpace(uninstallString))
                            continue;

                        Process.Start(new ProcessStartInfo
                        {
                            FileName = "cmd.exe",
                            Arguments = "/c " + uninstallString,
                            UseShellExecute = false,
                            CreateNoWindow = true
                        });

                        return;
                    }
                }
            }
        }

        MessageBox.Show(
            "No se encontró una instalación de Rellenador EX-17.",
            "Desinstalar Rellenador EX-17",
            MessageBoxButtons.OK,
            MessageBoxIcon.Information
        );
    }
}
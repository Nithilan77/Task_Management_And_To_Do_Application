# Helper: convert Markdown project report to DOCX using Pandoc
# Usage: Open PowerShell, run: .\convert_to_docx.ps1 -Input "..\PROJECT_REPORT_DETAILED.md" -Output "..\docs\report\PROJECT_REPORT_DETAILED.docx"
param(
    [string]$Input = "..\PROJECT_REPORT_DETAILED.md",
    [string]$Output = "PROJECT_REPORT_DETAILED.docx"
)

if (-not (Get-Command pandoc -ErrorAction SilentlyContinue)) {
    Write-Host "Pandoc not found on PATH. Install Pandoc first: https://pandoc.org/installing.html or use Chocolatey: choco install pandoc -y"
    exit 1
}

pandoc $Input -o $Output --from markdown+yaml_metadata_block --toc
Write-Host "Created $Output"
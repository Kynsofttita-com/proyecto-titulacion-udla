#!/usr/bin/env python3
"""
Convertir SVGs a PNG para incluir en PDF
"""

from pathlib import Path
import cairosvg

def convertir_svg_a_png():
    """Convertir los 2 diagramas principales a PNG"""

    diagrama_path = Path(__file__).parent

    diagramas = [
        'diagrama-1-contexto-sistema.svg',
        'diagrama-2-procesos-principales.svg'
    ]

    print("Convirtiendo SVGs a PNG...\n")

    for diagrama in diagramas:
        svg_path = diagrama_path / diagrama
        png_path = diagrama_path / diagrama.replace('.svg', '.png')

        if svg_path.exists():
            try:
                cairosvg.svg2png(
                    url=str(svg_path),
                    write_to=str(png_path),
                    dpi=300  # Alta resolucion
                )
                tamaño = png_path.stat().st_size / 1024
                print(f"[OK] {diagrama} -> {diagrama.replace('.svg', '.png')} ({tamaño:.1f} KB)")
            except Exception as e:
                print(f"[ERROR] {diagrama}: {e}")
        else:
            print(f"[ERROR] {diagrama} no encontrado")

    print("\n[SUCCESS] Conversion completada")

if __name__ == '__main__':
    convertir_svg_a_png()

/**
 * Piezas comunes a todos los módulos: base del dominio, puertos transversales y adaptadores técnicos.
 * Es un módulo abierto: cualquier módulo puede usarlo, pero él no conoce a ninguno.
 */
@ApplicationModule(displayName = "Compartido", type = ApplicationModule.Type.OPEN)
package com.terracodex.shared;

import org.springframework.modulith.ApplicationModule;

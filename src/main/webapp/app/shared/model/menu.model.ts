export interface IMenu {
  id?: number;
  label?: string;
  name?: string;
  position?: number;
  level?: number;
  active?: boolean;
}

export class Menu implements IMenu {
  constructor(
    public id?: number,
    public label?: string,
    public name?: string,
    public position?: number,
    public level?: number,
    public active?: boolean
  ) {
    this.active = this.active || false;
  }
}
